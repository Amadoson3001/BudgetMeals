@echo off
call gradlew.bat :app:assembleDebug :app:lintDebug --console=plain
exit /b %ERRORLEVEL%
