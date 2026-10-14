Set-Location $PSScriptRoot
& .\gradlew.bat :app:testDebugUnitTest :app:lintDebug --console=plain
$code = $LASTEXITCODE
Write-Output "TESTS_EXIT=$code"
exit $code
