@echo off
setlocal EnableExtensions
cd /d "%~dp0"
call "%~dp0oyl.bat" estado
exit /b %errorlevel%
