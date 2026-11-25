@echo off
setlocal EnableExtensions
cd /d "%~dp0"
if not "%~1"=="" goto accion
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\servicios.ps1" -Accion ayuda
exit /b %errorlevel%
:accion
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\servicios.ps1" -Accion "%~1"
exit /b %errorlevel%
