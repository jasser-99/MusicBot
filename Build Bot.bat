@echo off
cd /d "%~dp0"
set "JAVA_HOME=%~dp0.runtime"
set "PATH=%JAVA_HOME%\bin;%PATH%"
call .tools\maven\bin\mvn.cmd verify -B
if errorlevel 1 (
  pause
  exit /b 1
)
copy /y target\JMusicBot-0.7.0-All.jar JMusicBot.jar
pause
