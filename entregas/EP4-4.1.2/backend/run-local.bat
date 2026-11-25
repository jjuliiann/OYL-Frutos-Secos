@echo off
setlocal EnableExtensions
cd /d "%~dp0"
call "%~dp0oyl.bat" "%~1"
exit /b %errorlevel%
