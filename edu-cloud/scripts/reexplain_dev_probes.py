"""Dev-runtime verification probes for POST /api/v1/tutor/reexplain (Task 8).

Boots the local Django dev server twice (disabled default, then fixture mode)
and records REAL HTTP request/response evidence for the acceptance record.
"""

import json
import os
import subprocess
import sys
import time
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

PORT = 8765
BASE = f"http://127.0.0.1:{PORT}"
ENV = {**os.environ, "DJANGO_SECRET_KEY": "dev-probe-secret-not-for-production"}

GOOD = {
    "consent": True,
    "learner_id": "c0a80101-7a7c-4e10-9e2a-f4f1ef2d6a01",
    "lesson_id": "g3-t1-w2-l1-counting-twos",
    "analogy_domain": "sports",
    "source_excerpt": "Count in twos from any number. After 608 comes 610, 612, 614.",
    "verified_answer": "610, 612, 614",
}


def call(path, payload=None):
    body = json.dumps(payload).encode() if payload is not None else None
    req = Request(BASE + path, data=body, method="POST", headers={"Content-Type": "application/json"})
    try:
        with urlopen(req, timeout=10) as resp:
            return resp.status, json.loads(resp.read().decode())
    except HTTPError as exc:
        raw = exc.read().decode()
        try:
            return exc.code, json.loads(raw)
        except json.JSONDecodeError:
            return exc.code, {"raw": raw[:120]}


def wait_ready(timeout=30):
    deadline = time.time() + timeout
    while time.time() < deadline:
        try:
            with urlopen(BASE + "/api/v1/health", timeout=2) as resp:
                if resp.status == 200:
                    return True
        except (URLError, OSError):
            time.sleep(0.4)
    return False


def run_server(label, extra_env, probes):
    env = {**ENV, **extra_env}
    proc = subprocess.Popen(
        [sys.executable, "manage.py", "runserver", f"127.0.0.1:{PORT}", "--noreload"],
        env=env,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )
    print(f"\n=== SERVER [{label}] REEXPLAIN_MODE={extra_env.get('REEXPLAIN_MODE', '(default)')} ===")
    try:
        assert wait_ready(), f"server [{label}] did not become healthy"
        for name, path, payload in probes:
            status, body = call(path, payload)
            print(f"PROBE {name}: HTTP {status} :: {json.dumps(body)[:220]}")
    finally:
        proc.terminate()
        proc.wait(timeout=15)


run_server("A-default-disabled", {}, [
    ("disabled-mode-post", "/api/v1/tutor/reexplain", GOOD),
])

run_server("B-fixture-enabled", {"REEXPLAIN_MODE": "fixture"}, [
    ("happy-path-first", "/api/v1/tutor/reexplain", GOOD),
    ("happy-path-cached", "/api/v1/tutor/reexplain", GOOD),
    ("unconsented", "/api/v1/tutor/reexplain", {**GOOD, "consent": False}),
    ("unknown-domain", "/api/v1/tutor/reexplain", {**GOOD, "analogy_domain": "astronomy"}),
    ("oversized-body", "/api/v1/tutor/reexplain", {**GOOD, "source_excerpt": "x" * 900}),
])

print("\nALL PROBES COMPLETE")
