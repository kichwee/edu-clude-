<#!
.SYNOPSIS
Runs the reproducible local checks for the checked-in EduCloud demo.

.DESCRIPTION
This verifies that Android's generated demo catalogue matches the canonical
JSON pack, then runs backend checks/tests and Android unit tests. It never
contacts a telephony provider or reads credentials beyond DJANGO_SECRET_KEY.
#>
param()

$ErrorActionPreference = 'Stop'
function Assert-NativeSuccess([string]$step) {
    if ($LASTEXITCODE -ne 0) { throw "$step failed with exit code $LASTEXITCODE." }
}
$workspace = Split-Path -Parent $PSScriptRoot
$python = Join-Path $workspace 'edu-cloud\backend\.venv\Scripts\python.exe'
if (-not (Test-Path -LiteralPath $python)) { throw 'Create edu-cloud/backend/.venv before running this verifier.' }

$generated = Join-Path $workspace 'android-app\app\src\main\java\com\example\educloud\content\GeneratedGrade3RuleTutor.kt'
if (-not (Test-Path -LiteralPath $generated)) { throw 'The Android catalogue was not generated.' }
$before = Get-Content -LiteralPath $generated -Raw
& $python (Join-Path $workspace 'tools\generate_grade3_rule_tutor.py')
Assert-NativeSuccess 'Content generation'
$after = Get-Content -LiteralPath $generated -Raw
if ($before -ne $after) { throw 'GeneratedGrade3RuleTutor.kt was stale. Review it, then rerun verification.' }

$env:DJANGO_SECRET_KEY = 'local-verification-secret-not-for-production'
Push-Location (Join-Path $workspace 'edu-cloud\backend')
try {
    & .\.venv\Scripts\python.exe manage.py check
    Assert-NativeSuccess 'Django system check'
    & .\.venv\Scripts\python.exe manage.py test educloud.tests -v 1
    Assert-NativeSuccess 'Backend tests'
} finally { Pop-Location }

$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
Push-Location (Join-Path $workspace 'android-app')
try {
    & .\gradlew.bat testDebugUnitTest --offline --no-daemon --console=plain
    Assert-NativeSuccess 'Android unit tests'
} finally { Pop-Location }
