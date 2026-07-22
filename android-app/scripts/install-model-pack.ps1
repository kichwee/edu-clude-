<#
Copies the already verified LiteRT-LM model pack into the debug app's private
files directory for a future experimental build. The active Grade 3 Maths MVP
does not package the LiteRT runtime or run this model after its failed quality
evaluation. This helper does not enable the model in the shipped app.
#>
param(
    [string]$AdbPath = "${env:LOCALAPPDATA}\Android\Sdk\platform-tools\adb.exe",
    [string]$ModelPath = "$PSScriptRoot\..\model-packs\SmolLM2_360M_instruct.litertlm",
    [string]$PackageName = "com.example.educloud.debug"
)

$ErrorActionPreference = "Stop"
$targetRelativePath = "files/models/smollm2-360m-instruct.litertlm"
$temporaryDevicePath = "/sdcard/Download/SmolLM2_360M_instruct.litertlm"

if (-not (Test-Path -LiteralPath $AdbPath)) {
    throw "adb was not found at '$AdbPath'. Install Android platform-tools or pass -AdbPath."
}
if (-not (Test-Path -LiteralPath $ModelPath)) {
    throw "Verified model pack was not found at '$ModelPath'."
}

& $AdbPath get-state | Out-Null
& $AdbPath push $ModelPath $temporaryDevicePath
& $AdbPath shell run-as $PackageName mkdir -p files/models
& $AdbPath shell run-as $PackageName cp $temporaryDevicePath $targetRelativePath
& $AdbPath shell rm -f $temporaryDevicePath

Write-Host "Model pack installed for $PackageName at $targetRelativePath."
