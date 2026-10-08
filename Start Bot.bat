@echo off
cd /d "%~dp0"
if not exist config.txt (
  echo Run Setup Bot.bat first.
  pause
  exit /b 1
)
if not exist .runtime\bin\java.exe (
  echo The portable Java runtime is missing. See LOCAL-SETUP.md.
  pause
  exit /b 1
)
if not exist JMusicBot.jar (
  echo The compiled bot is missing. Run Build Bot.bat first.
  pause
  exit /b 1
)
echo Starting MusicBot. Use Ctrl+C in this window to stop.
.runtime\bin\java.exe -Dfile.encoding=UTF-8 -Dnogui=true --enable-native-access=ALL-UNNAMED -Xms128m -Xmx512m -XX:+UseZGC -jar JMusicBot.jar
pause
