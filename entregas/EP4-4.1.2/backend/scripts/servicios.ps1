param([ValidateSet('ayuda','eureka','usuarios','productos','pedidos','gateway','estado','detener','java')][string]$Accion='ayuda')
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $root
$logs=Join-Path $root 'logs'
$runtime=Join-Path $root 'runtime'
$modules=[ordered]@{'eureka-server'=8761;'user-service'=8081;'product-service'=8082;'order-service'=8083;'api-gateway'=8080}
$ProgressPreference='SilentlyContinue'
$windowsHost=[IO.Path]::DirectorySeparatorChar -eq '\'
$binName=if ($windowsHost) { 'java.exe' } else { 'java' }
function Get-OylJavaInfo([string]$exe) {
    # Leemos la salida completa y el código de salida del proceso ya terminado.
    $pi = New-Object System.Diagnostics.ProcessStartInfo
    $pi.FileName = $exe
    $pi.Arguments = '-XshowSettings:properties -version'
    $pi.UseShellExecute = $false
    $pi.RedirectStandardOutput = $true
    $pi.RedirectStandardError = $true
    $pi.CreateNoWindow = $true
    $p = New-Object System.Diagnostics.Process
    $p.StartInfo = $pi
    try {
        if (!$p.Start()) { return }
        $stdout = $p.StandardOutput.ReadToEndAsync()
        $stderr = $p.StandardError.ReadToEndAsync()
        if (!$p.WaitForExit(8000)) {
            try { $p.Kill() } catch {}
            return
        }
        if ($p.ExitCode -ne 0) { return }
        $report = $stderr.Result + [Environment]::NewLine + $stdout.Result
        $majorMatch = [regex]::Match($report, '(?m)^\s*java\.specification\.version\s*=\s*(?:1\.)?(\d+)\b')
        if (!$majorMatch.Success) { return }
        # Si el PATH lleva a un acceso de Oracle, usa el ejecutable real del JDK.
        $homeMatch = [regex]::Match($report, '(?m)^\s*java\.home\s*=\s*([^\r\n]+)')
        if ($homeMatch.Success) {
            $real = Join-Path $homeMatch.Groups[1].Value.Trim() ("bin/"+$binName)
            if (Test-Path -LiteralPath $real -PathType Leaf) { $exe = $real }
        }
        [pscustomobject]@{ Major = [int]$majorMatch.Groups[1].Value; Exe = $exe }
    } catch { return } finally { $p.Dispose() }
}
function Find-OylJava {
    # JAVA_HOME, PATH, Java de Android Studio y directorios habituales de JDK.
    $options = @(
        if ($env:JAVA_HOME) { Join-Path ($env:JAVA_HOME.Trim().Trim('"')) ("bin/"+$binName) }
        Get-Command $binName -All -CommandType Application -ErrorAction SilentlyContinue | ForEach-Object { $_.Source }
        if ($env:ProgramFiles) { Join-Path $env:ProgramFiles 'Android/Android Studio/jbr/bin/java.exe' }
        if ($env:LOCALAPPDATA) { Join-Path $env:LOCALAPPDATA 'Programs/Android Studio/jbr/bin/java.exe' }
        $roots = @(
            if ($env:ProgramFiles) {
                foreach ($vendor in 'Microsoft','Eclipse Adoptium','Java','Amazon Corretto') { Join-Path $env:ProgramFiles $vendor }
            }
            if ($env:USERPROFILE) { Join-Path $env:USERPROFILE '.jdks' }
        )
        foreach ($dir in $roots) {
            if (Test-Path -LiteralPath $dir -PathType Container) {
                Get-ChildItem -LiteralPath $dir -Directory -ErrorAction SilentlyContinue |
                    ForEach-Object { Join-Path $_.FullName 'bin/java.exe' }
            }
        }
    )
    $tried = @()
    foreach ($candidate in ($options | Select-Object -Unique)) {
        if (!(Test-Path -LiteralPath $candidate -PathType Leaf)) { continue }
        $found = Get-OylJavaInfo $candidate
        if ($found -and $found.Major -ge 21) { return $found }
        if ($found) { $tried += "Java $($found.Major): $candidate" } else { $tried += "No responde: $candidate" }
    }
    $reason = if ($tried.Count) { $tried -join '; ' } else { 'No se encontro java.exe.' }
    throw "No se encontro Java 21 o posterior. $reason Si no lo tienes, instala el JDK en CMD: winget install --id Microsoft.OpenJDK.21 -e"
}
# Cada acción de inicio corresponde exactamente a un servicio.
$names = [ordered]@{eureka='eureka-server'; usuarios='user-service'; productos='product-service'; pedidos='order-service'; gateway='api-gateway'}
function Show-Help {
    Write-Host 'OYL Frutos Secos - arranque manual. Este mensaje no inicia ningun servicio.'
    Write-Host 'Desde esta misma CMD, ejecuta una linea por vez y espera su OK:'
    foreach ($key in $names.Keys) { Write-Host "  oyl.bat $key" }
    Write-Host '  oyl.bat estado     (registro y rutas HTTP)'
    Write-Host '  oyl.bat detener    (al terminar, cierra solo esta copia)'
}
function Jar-Path($module) { Join-Path $runtime "$module-1.0.0.jar" }
function Record-Path($module) { Join-Path $logs "$module.manual.json" }
function Owned($module) {
    $record=Record-Path $module
    if (!(Test-Path -LiteralPath $record)) { return }
    try {
        $obj=Get-Content -LiteralPath $record -Raw | ConvertFrom-Json
        $jar=Jar-Path $module
        if ([string]$obj.jar -ne $jar) { return }
        $p=Get-Process -Id ([int]$obj.id) -ErrorAction Stop
        if ([Math]::Abs(($p.StartTime.ToUniversalTime() - [datetime]::FromFileTimeUtc([long]$obj.start)).TotalSeconds) -gt 2) { return }
        if ([string]$obj.tag -notmatch '^[a-f0-9]{32}$') { return }
        if ($windowsHost) {
            # Nunca enviamos una señal a un PID: la marca única pertenece solo al JAR de esta copia.
            if ($p.ProcessName -ine 'java') { return }
        } else {
            $args0=[IO.File]::ReadAllText("/proc/$($p.Id)/cmdline").Split([char]0)
            if ($args0 -cnotcontains $jar) { return }
        }
        return $p
    } catch { return }
}
function Listening($port) {
    return @([Net.NetworkInformation.IPGlobalProperties]::GetIPGlobalProperties().GetActiveTcpListeners() | Where-Object {$_.Port -eq $port}).Count -gt 0
}
function Shared-Key {
    # Publicación atómica: dos ventanas no generan claves incompatibles.
    if ($env:OYL_JWT_SECRET) { return }
    $data=Join-Path $root 'data'
    [void][IO.Directory]::CreateDirectory($data)
    $secretFile=Join-Path $data '.jwt-secret'
    if (!(Test-Path -LiteralPath $secretFile)) {
        $temp=Join-Path $data ([guid]::NewGuid().ToString('N')+'.tmp')
        $bytes=New-Object byte[] 48
        $rng=[Security.Cryptography.RandomNumberGenerator]::Create()
        try {
            $rng.GetBytes($bytes)
            [IO.File]::WriteAllText($temp,[Convert]::ToBase64String($bytes))
            try { [IO.File]::Move($temp,$secretFile) } catch [IO.IOException] {
                if (!(Test-Path -LiteralPath $secretFile)) { throw }
            }
        } finally { $rng.Dispose(); if (Test-Path -LiteralPath $temp) { [IO.File]::Delete($temp) } }
    }
    $env:OYL_JWT_SECRET=[IO.File]::ReadAllText($secretFile).Trim()
}
function Endpoint($url) { Invoke-RestMethod -Uri $url -TimeoutSec 3 -ErrorAction Stop }
function Alive($module) {
    $origin="http://127.0.0.1:$($modules[$module])"
    if ($module -eq 'eureka-server') { return ((Invoke-WebRequest -UseBasicParsing "$origin/" -TimeoutSec 3).StatusCode -eq 200) }
    if ($module -eq 'api-gateway') { return ((Endpoint "$origin/actuator/health").status -eq 'UP') }
    $expected=@{'user-service'='users';'product-service'='products';'order-service'='orders'}[$module]
    $r=Endpoint "$origin/api/health"; return ($r.status -eq 'ok' -and $r.service -eq $expected)
}
function Routes {
    $registry=Invoke-RestMethod 'http://127.0.0.1:8761/eureka/apps' -Headers @{Accept='application/json'} -TimeoutSec 3 -ErrorAction Stop
    foreach ($module in 'user-service','product-service','order-service','api-gateway') {
        $up=@($registry.applications.application | Where-Object {$_.name -ieq "OYL-$module"} | ForEach-Object {$_.instance} | Where-Object {$_.status -eq 'UP'})
        if (!$up.Count) { return $false }
    }
    foreach ($name in 'users','products','orders') {
        $r=Endpoint "http://127.0.0.1:8080/api/health/$name"
        if ($r.status -ne 'ok' -or $r.service -ne $name) { return $false }
    }
    $null=Endpoint 'http://127.0.0.1:8080/api/products'
    $null=Endpoint 'http://127.0.0.1:8080/api/locations'
    return $true
}
function Show-State {
    $ready=$true
    foreach ($key in $names.Keys) {
        $module=$names[$key]; $ok=$false
        try { if (Listening $modules[$module]) { $ok=Alive $module } } catch {}
        if ($ok) { Write-Host "OK         $module : $($modules[$module])" }
        else { Write-Host "SIN RESPUESTA  $module : $($modules[$module])"; $ready=$false }
    }
    if (!$ready) { Write-Host 'Inicia manualmente los que faltan; estado no arranca servidores.'; return $false }
    Write-Host 'Verificando registro en Eureka y rutas reales del Gateway...'
    $t=[Diagnostics.Stopwatch]::StartNew()
    do {
        try {
            if (Routes) {
                Write-Host 'BACKEND LISTO: 4 instancias UP; usuarios, productos, pedidos, catalogo y retiros responden por el Gateway.'
                Write-Host 'Eureka (PC): http://localhost:8761/'
                Write-Host 'Catalogo (PC): http://localhost:8080/api/products'
                Write-Host 'Android Emulator: http://10.0.2.2:8080/api/'
                return $true
            }
        } catch {}
        Start-Sleep -Milliseconds 1000
    } while ($t.Elapsed.TotalSeconds -lt 90)
    Write-Host 'Los puertos responden, pero todavia falla el registro o una ruta del Gateway. Revisa logs y repite oyl.bat estado.'
    return $false
}
function Start-One($module) {
    if (Owned $module) { Write-Host "Esta copia ya inicio $module. No se lanzo otra instancia. Para verificar: oyl.bat estado"; return }
    if (Listening $modules[$module]) { throw "El puerto $($modules[$module]) esta ocupado. Deten la copia anterior (o Docker) antes de iniciar $module." }
    $jar=Jar-Path $module
    if (!(Test-Path -LiteralPath $jar -PathType Leaf)) { throw "Falta $jar. Extrae todo el ZIP; si cambiaste los fuentes usa compilar-backend.bat." }
    $j=Find-OylJava; Write-Host "Java $($j.Major): $($j.Exe)"
    Shared-Key; [void][IO.Directory]::CreateDirectory($logs)
    $logFile=Join-Path $logs "$module.log"
    $control=Join-Path $PSScriptRoot 'cli/cierre-cmd.jar'
    if (!(Test-Path -LiteralPath $control -PathType Leaf)) { throw 'Falta scripts/cli/cierre-cmd.jar: extrae el ZIP completo.' }
    $tag=[guid]::NewGuid().ToString('N')
    $stopfile=Join-Path $logs "$module.$tag.stop"
    $info=New-Object Diagnostics.ProcessStartInfo
    $info.FileName=$j.Exe
    $info.Arguments='-Xmx256m "-Doyl.cmd.stop='+$stopfile+'" "-javaagent:'+$control+'" -jar "'+$jar+'" "--logging.file.name='+$logFile+'"'
    $info.WorkingDirectory=$root; $info.UseShellExecute=$false; $info.CreateNoWindow=$false
    # La JVM escribe en la misma consola que CMD; solo se aísla su entrada para seguir escribiendo.
    $info.RedirectStandardInput=$true
    Write-Host ("EJECUTANDO: " + '"' + $j.Exe + '" ' + $info.Arguments)
    $p=[Diagnostics.Process]::Start($info)
    try {
        $p.StandardInput.Close()
        $record=@{id=$p.Id; start=$p.StartTime.ToUniversalTime().ToFileTimeUtc().ToString(); jar=$jar; tag=$tag}
        [IO.File]::WriteAllText((Record-Path $module),($record | ConvertTo-Json -Compress))
        Write-Host "PID $($p.Id) | $module | puerto $($modules[$module])"
        $t=[Diagnostics.Stopwatch]::StartNew()
        do {
            if ($p.HasExited) { throw "$module termino con codigo $($p.ExitCode). Revisa el mensaje anterior o logs/$module.log." }
            try { $ok=Alive $module } catch { $ok=$false }
            if ($ok -and !$p.HasExited) { Write-Host "OK: $module responde en $($modules[$module]). Vuelve a CMD; la JVM continua activa."; return }
            Start-Sleep -Milliseconds 700
        } while ($t.Elapsed.TotalSeconds -lt 150)
        throw "Aun no responde $module; la JVM puede seguir en arranque. Ejecuta oyl.bat estado antes de repetir. Registro: logs/$module.log."
    } finally { $p.Dispose() }
}
function Stop-Owned {
    $total=0; $left=@()
    foreach ($module in 'api-gateway','order-service','product-service','user-service','eureka-server') {
        $p=Owned $module
        if (!$p) { continue }
        $total++
        $p.Dispose()
        $record=Record-Path $module
        $obj=Get-Content -LiteralPath $record -Raw | ConvertFrom-Json
        if ([string]$obj.tag -notmatch '^[a-f0-9]{32}$') { throw "Registro invalido: $module" }
        $signalFile=Join-Path $logs "$module.$($obj.tag).stop"
        Write-Host "Solicitando cierre de $module..."
        [IO.File]::WriteAllText($signalFile,'cerrar')
        $t=[Diagnostics.Stopwatch]::StartNew()
        while ((Owned $module) -and $t.Elapsed.TotalSeconds -lt 30) { Start-Sleep -Milliseconds 350 }
        if (Owned $module) { $left+=$module; continue }
        Remove-Item -LiteralPath $record -ErrorAction SilentlyContinue
        Remove-Item -LiteralPath $signalFile -ErrorAction SilentlyContinue
        Write-Host "Detenido: $module"
    }
    if (!$total) { Write-Host 'Esta copia no tiene procesos propios activos.' }
    if ($left.Count) { throw ('Aun activos: ' + ($left -join ', ') + '. Consulta sus logs. No se ha forzado ni cerrado ningun proceso ajeno.') }

}
try {
    if ($Accion -eq 'ayuda') { Show-Help; exit 0 }
    if ($Accion -eq 'estado') { if (Show-State) { exit 0 } else { exit 2 } }
    if ($Accion -eq 'detener') { Stop-Owned; exit 0 }
    if ($Accion -eq 'java') { $j=Find-OylJava; Write-Host "Java $($j.Major): $($j.Exe)"; exit 0 }
    Start-One $names[$Accion]; exit 0
} catch { Write-Host ("ERROR: " + $_.Exception.Message) -ForegroundColor Red; exit 1 }
