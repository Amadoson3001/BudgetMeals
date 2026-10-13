@echo off
call gradlew.bat :app:assembleDebug
if %errorlevel%==0 adb install -r app\build\outputs\apk\debug\app-debug.apk
