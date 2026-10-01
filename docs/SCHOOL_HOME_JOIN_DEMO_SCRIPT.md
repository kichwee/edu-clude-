# School–home join — 5-minute demo script

> 25 August 2026. Use with [SCHOOL_HOME_JOIN_PRESENTER_PRD.md](SCHOOL_HOME_JOIN_PRESENTER_PRD.md).  
> Do-not-say list is slide 0. If you say a banned word, stop and correct.

**This is a labelled prototype.** It is not Athena production, not a live join of two repos, and not facial analysis.

---

## Before the room (2 minutes)

1. Backend (PowerShell, from `edu-cloud/backend`):

```powershell
$env:DJANGO_SECRET_KEY = 'replace-with-a-new-local-secret'
.\.venv\Scripts\python.exe manage.py runserver
```

2. Open [http://127.0.0.1:8000/demo/teacher/sidekick](http://127.0.0.1:8000/demo/teacher/sidekick). Confirm the orange banner.
3. Android app on a device or emulator (onboarding completed). Optional live POST from the phone:

```powershell
cd android-app
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat -PhomeworkBaseUrl=http://10.0.2.2:8000 assembleDebug
```

If that URL is not set, the phone still opens the **bundled fixture**; use **Record a demo attempt** on the teacher page for item counts.

4. Optional Beat 0: Athena USB/desktop on a second laptop. If you do not have it, skip Beat 0. Do not pretend EduCloud is Athena.

---

## Speaker notes (about 5:00)

| Time | You say | You show | You do **not** say |
|---|---|---|---|
| 0:00 | “Two products, one homework pack, no cameras.” Point at the do-not-say list. | Printed list or first slide | Facial recognition, brain, TRIBE |
| 0:35 | “Athena is the USB classroom practice system for JSS. EduCloud is the phone revision companion. They are not one app today.” | Architecture slide | “Already one system” |
| 1:10 | **If Athena desktop is in the room:** “This is their practice app and read-only teacher analytics. Teachers cannot assign yet — that is their Phase B.” | Athena app | “Athena already pushes homework to phones” |
| 1:10 | **If not:** “School side today is their site and PRD. Teacher direction is unbuilt. We prototype only the home half.” | athenalearn.org prospectus | Inflated school count |
| 2:00 | “Teacher sidekick is a contract prototype on EduCloud.” Click **Assign tonight’s revision**. Class code **G3-HOME**. | Browser banner + code | “This is Athena production” |
| 2:40 | Phone: **Tonight from class** → **Use demo assignment** (or type `G3-HOME`) → **Do the quiz**. Three regrouping items. | Android | “We can see who is sleeping” |
| 3:40 | **Ask the tutor** → wait for the regrouping prompt → tap **Explain it my way** if the chip is enabled; otherwise the local story still works. | Chat | “Offline AI on every DLP tablet” |
| 4:20 | Teacher page: **Record a demo attempt** (or wait if the phone posted). “Learners attempted, items missed — not faces.” | Item table | Names, photos, engagement scores |
| 4:45 | “What is not built: Athena assignment, KICD approval, Grade 7 in this app, cameras.” One ask only. | Honest list | “KICD-approved” |

---

## If something breaks

- Teacher page 500: `DJANGO_SECRET_KEY` is missing.
- Phone cannot fetch `G3-HOME`: use **Use demo assignment**; still a valid home beat.
- Explain it my way disabled: no interest chips or reexplain mode off — the deterministic story is the fallback. Say that.
- Someone asks about smart boards: “Most boards are a Windows PC. Athena already targets that PC. No vendor SDK in this demo.”
