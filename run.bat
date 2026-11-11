@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"

java -cp out Main %*

exit /b %errorlevel%
