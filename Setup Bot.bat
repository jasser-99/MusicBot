@echo off
cd /d "%~dp0"
powershell.exe -NoProfile -File "%~dp0Setup-Bot.ps1"
pause
