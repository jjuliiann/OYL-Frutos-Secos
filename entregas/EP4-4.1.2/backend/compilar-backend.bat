@echo off
setlocal EnableExtensions
cd /d "%~dp0"
call mvn clean verify
if errorlevel 1 exit /b 1
if not exist runtime mkdir runtime
for %%M in (eureka-server user-service product-service order-service api-gateway) do (
    copy /y "%%M\target\%%M-1.0.0.jar" "runtime\%%M-1.0.0.jar" >nul
    if errorlevel 1 exit /b 1
)
echo Compilacion y pruebas completadas. JAR actualizados en runtime.
exit /b 0
