Set-Location $PSScriptRoot
& .\gradlew.bat :app:assembleDebug --console=plain 2>&1 | Tee-Object build_run.log
if ($LASTEXITCODE -eq 0) { adb install -r app\build\outputs\apk\debug\app-debug.apk }
