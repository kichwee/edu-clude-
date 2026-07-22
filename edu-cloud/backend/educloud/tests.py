from django.core.cache import cache
from django.test import SimpleTestCase, TestCase, override_settings
from types import SimpleNamespace
from unittest.mock import patch
from uuid import UUID

from .grade3_rule_tutor import LESSONS


class DemoApiTests(SimpleTestCase):
    def test_health_is_available(self):
        response = self.client.get("/api/v1/health")
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()["status"], "ok")

    def test_ussd_quiz_correct_answer(self):
        response = self.client.post("/api/v1/demo/ussd", {"text": "2*2"})
        self.assertEqual(response.status_code, 200)
        self.assertTrue(response.content.decode().startswith("END Nice work"))

    def test_rule_based_grade_three_tutor_is_source_backed_and_never_uses_free_text(self):
        menu = self.client.post("/api/v1/demo/ussd", {"text": "1"})
        lesson = self.client.post("/api/v1/demo/ussd", {"text": "1*2"})
        hint = self.client.post("/api/v1/demo/ussd", {"text": "1*2*1"})
        question = self.client.post("/api/v1/demo/ussd", {"text": "1*2*2"})
        correct = self.client.post("/api/v1/demo/ussd", {"text": "1*2*2*1"})

        self.assertIn("Grade 3 Maths tutor", menu.content.decode())
        self.assertIn("Counting in twos", lesson.content.decode())
        self.assertIn("Book p.14", lesson.content.decode())
        self.assertIn("Hint: Find the change", hint.content.decode())
        self.assertIn("610, 612, 614", question.content.decode())
        self.assertTrue(correct.content.decode().startswith("END Nice work!"))

    def test_rule_based_grade_three_tutor_rejects_invalid_menu_input(self):
        response = self.client.post("/api/v1/demo/ussd", {"text": "1*9"})
        self.assertEqual("END That topic is unavailable. Dial again.", response.content.decode())

    def test_shared_rule_pack_contract_has_stable_ids_sources_answers_and_version(self):
        self.assertEqual(
            [
                "g3-t1-w1-l1-position",
                "g3-t1-w2-l1-counting-twos",
                "g3-t1-w2-l2-tens-ones",
                "g3-t1-w2-l4-number-words",
            ],
            [lesson.id for lesson in LESSONS],
        )
        self.assertTrue(all(lesson.version == "grade3-rule-based-demo-0.4" for lesson in LESSONS))
        self.assertTrue(all(lesson.source and lesson.correction and len(lesson.options) == 3 for lesson in LESSONS))

    def test_ussd_root_has_only_the_grade_three_mvp_and_quiz(self):
        menu = self.client.post("/api/v1/demo/ussd", {"text": ""})
        self.assertIn("Grade 3 Maths tutor", menu.content.decode())
        self.assertNotIn("Other demo lessons", menu.content.decode())
        removed = self.client.post("/api/v1/demo/ussd", {"text": "4"})
        self.assertEqual("END That option is unavailable. Dial again.", removed.content.decode())

    def test_sms_stop_is_simulated_and_does_not_dispatch(self):
        response = self.client.post("/api/v1/demo/sms", {"text": "STOP"})
        self.assertEqual(response.status_code, 200)
        self.assertTrue(response.json()["simulated"])
        self.assertIn("No messages", response.json()["reply"])

    def test_json_simulator_requests_do_not_require_a_csrf_cookie(self):
        response = self.client.post(
            "/api/v1/demo/sms",
            data='{"text": "MATH"}',
            content_type="application/json",
        )
        self.assertEqual(response.status_code, 200)
        self.assertIn("4 groups", response.json()["reply"])

    def test_simulators_reject_invalid_or_oversized_json(self):
        invalid = self.client.post(
            "/api/v1/demo/sms", data="[]", content_type="application/json"
        )
        oversized = self.client.post("/api/v1/demo/ussd", {"text": "x" * 65})
        self.assertEqual(invalid.status_code, 400)
        self.assertEqual(oversized.status_code, 400)

    def test_sandbox_callback_is_disabled_by_default(self):
        response = self.client.post(
            "/api/v1/sandbox/ussd/not-a-real-callback-token", {"text": "2*2"}
        )
        self.assertEqual(response.status_code, 404)

    @override_settings(
        TELEPHONY_MODE="sandbox",
        USSD_SANDBOX_CALLBACK_TOKEN="z" * 32,
    )
    def test_sandbox_callback_uses_only_text_and_never_needs_phone_data(self):
        response = self.client.post(
            "/api/v1/sandbox/ussd/" + "z" * 32,
            {
                "text": "2*2",
                "phoneNumber": "+254700000000",
                "sessionId": "provider-session-id",
                "serviceCode": "*123#",
            },
        )
        self.assertEqual(response.status_code, 200)
        self.assertTrue(response.content.decode().startswith("END Nice work"))

    @override_settings(
        TELEPHONY_MODE="sandbox",
        USSD_SANDBOX_CALLBACK_TOKEN="z" * 32,
    )
    def test_sandbox_callback_rejects_an_incorrect_url_capability(self):
        response = self.client.post(
            "/api/v1/sandbox/ussd/" + "y" * 32, {"text": "2*2"}
        )
        self.assertEqual(response.status_code, 404)

    def tearDown(self):
        cache.clear()
        super().tearDown()


@override_settings(EDGE_SYNC_MODE="demo", AGENT_SWARM_MODE="fixture")
class TeachingLoopApiTests(TestCase):
    """Contract tests for the consented, pseudonymous hackathon sync demo."""

    def test_failed_regrouping_attempts_create_and_return_a_personalized_rule_pack(self):
        learner_id = "c0a80101-7a7c-4e10-9e2a-f4f1ef2d6a01"
        request_id = "9430e213-1399-4655-aa4b-96ef6a36150f"
        telemetry = {
            "learner_id": learner_id,
            "request_id": request_id,
            "demo_consent": True,
            "attempts": [
                {
                    "attempt_id": "d62a1f6b-70af-4152-8c6d-f91c0d80e722",
                    "skill_id": "two_digit_subtraction_regrouping",
                    "minuend": 45,
                    "subtrahend": 29,
                    "learner_answer": 26,
                },
                {
                    "attempt_id": "09a5d7b0-633b-4a66-b6e2-d1d112f6d1fb",
                    "skill_id": "two_digit_subtraction_regrouping",
                    "minuend": 82,
                    "subtrahend": 37,
                    "learner_answer": 55,
                },
                {
                    "attempt_id": "d1ff54b8-ec7d-44d2-bbca-8645077e912b",
                    "skill_id": "two_digit_subtraction_regrouping",
                    "minuend": 63,
                    "subtrahend": 28,
                    "learner_answer": 45,
                },
            ],
        }

        created = self.client.post(
            "/api/v1/sync/telemetry",
            data=telemetry,
            content_type="application/json",
        )

        self.assertEqual(201, created.status_code)
        created_payload = created.json()
        self.assertEqual("ready", created_payload["status"])
        self.assertEqual("synchronous_demo", created_payload["processing_mode"])
        self.assertEqual("teacher_review_required", created_payload["review_status"])
        self.assertEqual(request_id, created_payload["request_id"])
        UUID(created_payload["pack_id"])

        downloaded = self.client.get(f"/api/v1/sync/remediation?learner_id={learner_id}")

        self.assertEqual(200, downloaded.status_code)
        pack = downloaded.json()["pack"]
        self.assertEqual("1", pack["schema_version"])
        self.assertEqual(created_payload["pack_id"], pack["pack_id"])
        self.assertEqual("two_digit_subtraction_regrouping", pack["target_skill"])
        self.assertEqual("teacher_review_required", pack["review_status"])
        self.assertEqual(3, len(pack["lessons"][0]["practice_questions"]))
        self.assertEqual(
            pack["lessons"][0]["practice_questions"][0]["minuend"]
            - pack["lessons"][0]["practice_questions"][0]["subtrahend"],
            pack["lessons"][0]["practice_questions"][0]["answer"],
        )

    def test_sync_requires_explicit_demo_consent(self):
        response = self.client.post(
            "/api/v1/sync/telemetry",
            data={
                "learner_id": "c0a80101-7a7c-4e10-9e2a-f4f1ef2d6a01",
                "request_id": "9430e213-1399-4655-aa4b-96ef6a36150f",
                "demo_consent": False,
                "attempts": [],
            },
            content_type="application/json",
        )

        self.assertEqual(400, response.status_code)
        self.assertIn("consent", response.json()["error"].lower())


class AgentSwarmTests(SimpleTestCase):
    @override_settings(
        AGENT_SWARM_MODE="openai",
        OPENAI_API_KEY="not-a-real-key",
        OPENAI_MODEL="gpt-4o-mini",
    )
    def test_openai_stages_are_mocked_and_compile_only_valid_math(self):
        from .agent_swarm import (
            Assessment,
            CompiledRemediationDraft,
            DifferentiatedLesson,
            PracticeQuestion,
            TelemetryRequest,
            run_teaching_loop,
        )

        assessment = Assessment(
            target_skill="two_digit_subtraction_regrouping",
            conceptual_gap="regrouping_from_tens",
            evidence_summary="The incorrect answers consistently skip the exchange from tens to ones before subtraction.",
            confidence=0.9,
        )
        lesson = DifferentiatedLesson(
            title="Trade one ten, then subtract",
            micro_lesson="Trade one ten for ten ones before you subtract the ones column. Then subtract the ones and the tens in order.",
            teaching_steps=["Check the ones", "Trade a ten", "Subtract and check"],
            definition="Regrouping trades one ten for ten ones before subtraction.",
        )
        compiled = CompiledRemediationDraft(
            title=lesson.title,
            micro_lesson=lesson.micro_lesson,
            teaching_steps=lesson.teaching_steps,
            definition=lesson.definition,
            practice_questions=[
                PracticeQuestion(minuend=45, subtrahend=29, answer=16),
                PracticeQuestion(minuend=82, subtrahend=37, answer=45),
                PracticeQuestion(minuend=63, subtrahend=28, answer=35),
            ],
        )
        responses = iter((assessment, lesson, compiled))
        parse_count = [0]

        def parse_response(**_kwargs):
            parse_count[0] += 1
            return SimpleNamespace(
                choices=[SimpleNamespace(message=SimpleNamespace(refusal=None, parsed=next(responses)))]
            )

        fake_client = SimpleNamespace(
            beta=SimpleNamespace(chat=SimpleNamespace(completions=SimpleNamespace(parse=parse_response)))
        )
        telemetry = TelemetryRequest.model_validate(
            {
                "learner_id": "c0a80101-7a7c-4e10-9e2a-f4f1ef2d6a01",
                "request_id": "9430e213-1399-4655-aa4b-96ef6a36150f",
                "demo_consent": True,
                "attempts": [
                    {"attempt_id": "d62a1f6b-70af-4152-8c6d-f91c0d80e722", "skill_id": "two_digit_subtraction_regrouping", "minuend": 45, "subtrahend": 29, "learner_answer": 26},
                    {"attempt_id": "09a5d7b0-633b-4a66-b6e2-d1d112f6d1fb", "skill_id": "two_digit_subtraction_regrouping", "minuend": 82, "subtrahend": 37, "learner_answer": 55},
                    {"attempt_id": "d1ff54b8-ec7d-44d2-bbca-8645077e912b", "skill_id": "two_digit_subtraction_regrouping", "minuend": 63, "subtrahend": 28, "learner_answer": 45},
                ],
            }
        )

        with patch("openai.OpenAI", return_value=fake_client):
            pack = run_teaching_loop(telemetry)

        self.assertEqual("teacher_review_required", pack.review_status)
        self.assertEqual(16, pack.lessons[0].practice_questions[0].answer)
        self.assertEqual(3, parse_count[0])
