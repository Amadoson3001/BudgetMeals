Set-Location $PSScriptRoot
& .\gradlew.bat :app:assembleDebug :app:lintDebug --console=plain
$code = $LASTEXITCODE
Write-Output "CHECKS_EXIT=$code"
exit $code
