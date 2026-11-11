
@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"

if not exist out mkdir out

javac -encoding UTF-8 --release 11 -Xlint:all -d out src\*.java

if errorlevel 1 goto compile_error

echo.
echo TEST 1
echo Command: run.bat --package express
call run.bat --package express

echo.
echo TEST 2
echo Command: run.bat --package express --max-depth 5
call run.bat --package express --max-depth 5

echo.
echo TEST 3
echo Command: run.bat --max-depth 3
call run.bat --max-depth 3

echo.
echo TEST 4
echo Command: run.bat --package ""
call run.bat --package ""

echo.
echo TEST 5
echo Command: run.bat --package "bad package"
call run.bat --package "bad package"

echo.
echo TEST 6
echo Command: run.bat --package "Express!"
call run.bat --package "Express!"

echo.
echo TEST 7
for /f "delims=" %%A in ('powershell -NoProfile -Command "'a' * 215"') do set "LONG_PACKAGE=%%A"
echo Command: run.bat --package [215 characters]
call run.bat --package "%LONG_PACKAGE%"

echo.
echo TEST 8
echo Command: run.bat --package express --repository https://registry.npmjs.org/
call run.bat --package express --repository https://registry.npmjs.org/

echo.
echo TEST 9
echo Command: run.bat --package express --repository invalid-url
call run.bat --package express --repository invalid-url

echo.
echo TEST 10
echo Command: run.bat --package express --repository ftp://registry.npmjs.org/
call run.bat --package express --repository ftp://registry.npmjs.org/

echo.
echo TEST 11
echo Command: run.bat --package express --max-depth 0
call run.bat --package express --max-depth 0

echo.
echo TEST 12
echo Command: run.bat --package express --max-depth -1
call run.bat --package express --max-depth -1

echo.
echo TEST 13
echo Command: run.bat --package express --max-depth abc
call run.bat --package express --max-depth abc

echo.
echo TEST 14
echo Command: run.bat --package express --test --repository tests/test-repository.json
call run.bat --package express --test --repository tests/test-repository.json

echo.
echo TEST 15
echo Command: run.bat --package express --test
call run.bat --package express --test

echo.
echo TESTING FINISHED
pause
exit /b 0

:compile_error
echo.
echo COMPILATION FAILED!
pause
exit /b 1
