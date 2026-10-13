@echo off
call gradlew.bat :app:testDebugUnitTest :app:lintDebug --console=plain
