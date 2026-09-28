[CmdletBinding()]
param([switch]$Foreground)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$envFile = Join-Path $root '.env.dev'
$containerName = 'garage-track-dev-postgres'
$volumeName = 'garage-track-dev-pgdata'
$database = 'garage_track_dev'
$user = 'garage_dev'
$port = 5434
$apiUrl = 'http://127.0.0.1:8080/api/vehicles'

Push-Location $root
try {
    if (-not (Test-Path $envFile)) {
        $existingContainer = docker ps -a --filter "name=^/$containerName$" --format '{{.Names}}'
        $existingVolume = docker volume ls --filter "name=^$volumeName$" --format '{{.Name}}'
        if ($existingContainer -or $existingVolume) {
            throw "Persistent database already exists but .env.dev is missing. Restore its credentials; no database was reset."
        }
        $bytes = New-Object byte[] 24
        $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
        try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
        $password = [BitConverter]::ToString($bytes).Replace('-', '')
        @(
            "DB_URL=jdbc:postgresql://127.0.0.1:$port/$database"
            "DB_USERNAME=$user"
            "DB_PASSWORD=$password"
        ) | Set-Content -LiteralPath $envFile -Encoding ascii
    }
    $settings = @{}
    Get-Content -LiteralPath $envFile | ForEach-Object {
        if ($_ -match '^([A-Za-z_][A-Za-z0-9_]*)=(.*)$') {
            $settings[$Matches[1]] = $Matches[2]
        }
    }
    if (-not $settings.DB_URL -or -not $settings.DB_USERNAME -or -not $settings.DB_PASSWORD) {
        throw '.env.dev must contain DB_URL, DB_USERNAME and DB_PASSWORD.'
    }
    if ($settings.DB_URL -ne "jdbc:postgresql://127.0.0.1:$port/$database" -or $settings.DB_USERNAME -ne $user) {
        throw '.env.dev endpoint does not match the isolated GarageTrack development database.'
    }
    docker info --format '{{.ServerVersion}}' | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Docker Desktop is not running.' }

    $existingContainer = docker ps -a --filter "name=^/$containerName$" --format '{{.Names}}'
    if (-not $existingContainer) {
        $env:POSTGRES_USER = $user
        $env:POSTGRES_DB = $database
        $env:POSTGRES_PASSWORD = $settings.DB_PASSWORD
        try {
            docker run -d --name $containerName --restart unless-stopped -p "127.0.0.1:${port}:5432" -v "${volumeName}:/var/lib/postgresql" -e POSTGRES_USER -e POSTGRES_DB -e POSTGRES_PASSWORD postgres:18.1-alpine | Out-Null
            if ($LASTEXITCODE -ne 0) { throw 'Could not create development PostgreSQL container.' }
        } finally {
            Remove-Item Env:POSTGRES_PASSWORD, Env:POSTGRES_USER, Env:POSTGRES_DB -ErrorAction SilentlyContinue
        }
    } else {
        $mounts = docker inspect $containerName --format '{{range .Mounts}}{{.Name}}:{{.Destination}}{{end}}'
        if ($mounts -ne "${volumeName}:/var/lib/postgresql") {
            throw "Container $containerName exists but uses unexpected storage. Refusing to use it."
        }
        docker start $containerName | Out-Null
        if ($LASTEXITCODE -ne 0) { throw 'Could not start development PostgreSQL container.' }
    }

    $ready = $false
    for ($i = 0; $i -lt 60; $i++) {
        docker exec $containerName pg_isready -q -U $user -d $database 2>$null
        if ($LASTEXITCODE -eq 0) { $ready = $true; break }
        Start-Sleep -Seconds 1
    }
    if (-not $ready) { throw 'Development PostgreSQL did not become ready.' }

    $runtimeDir = Join-Path $root '.local'
    New-Item -ItemType Directory -Path $runtimeDir -Force | Out-Null
    $pidFile = Join-Path $runtimeDir 'dev-server.pid'
    if (Test-Path $pidFile) {
        $oldPid = [int](Get-Content $pidFile -Raw)
        $oldProcess = Get-CimInstance Win32_Process -Filter "ProcessId = $oldPid"
        if ($oldProcess -and $oldProcess.CommandLine -like '*garage-track-dev.jar*') {
            Write-Host "Backend already running (PID $oldPid): $apiUrl"
            return
        }
    }
    $listener = Get-NetTCPConnection -State Listen -LocalPort 8080 -ErrorAction SilentlyContinue
    if ($listener) {
        $ownerProcess = Get-CimInstance Win32_Process -Filter "ProcessId = $($listener[0].OwningProcess)"
        if ($ownerProcess.CommandLine -like '*garage-track-dev.jar*') {
            Write-Host "Backend already running (PID $($ownerProcess.ProcessId)): $apiUrl"
            return
        }
        throw 'Port 8080 is already occupied. Not stopping an unrelated server.'
    }

    & (Join-Path $root 'mvnw.cmd') -DskipTests package
    if ($LASTEXITCODE -ne 0) { throw 'Maven package failed.' }
    $jar = Join-Path $root 'target\garage-track-api-0.0.1-SNAPSHOT.jar'
    if (-not (Test-Path $jar)) { throw "Application jar missing: $jar" }
    $runtimeJar = Join-Path $runtimeDir 'garage-track-dev.jar'
    Copy-Item -LiteralPath $jar -Destination $runtimeJar -Force

    $env:DB_URL = $settings.DB_URL
    $env:DB_USERNAME = $settings.DB_USERNAME
    $env:DB_PASSWORD = $settings.DB_PASSWORD
    try {
        $java = (Get-Command java.exe -ErrorAction Stop).Source
        if ($Foreground) {
            Write-Host "Starting backend in the current process; database persists on localhost:$port."
            & $java -jar $runtimeJar *> (Join-Path $runtimeDir 'dev-server.log')
            if ($LASTEXITCODE -ne 0) { throw "Backend exited with code $LASTEXITCODE." }
            return
        }
        $process = Start-Process -FilePath $java -ArgumentList @('-jar', "`"$runtimeJar`"") -WorkingDirectory $root -RedirectStandardOutput (Join-Path $runtimeDir 'dev-server.log') -RedirectStandardError (Join-Path $runtimeDir 'dev-server.err.log') -PassThru -WindowStyle Hidden
    } finally {
        Remove-Item Env:DB_URL, Env:DB_USERNAME, Env:DB_PASSWORD -ErrorAction SilentlyContinue
    }
    $process.Id | Set-Content -LiteralPath $pidFile
    for ($i = 0; $i -lt 60; $i++) {
        if ($process.HasExited) { throw "Backend exited during startup. See .local\dev-server.log (PID $($process.Id))." }
        try {
            $response = Invoke-WebRequest -Uri $apiUrl -UseBasicParsing -TimeoutSec 2
            if ($response.StatusCode -eq 200) {
                Write-Host "Development PostgreSQL persists in Docker volume $volumeName (localhost:$port)."
                Write-Host "Backend ready (PID $($process.Id)): $apiUrl"
                return
            }
        } catch { Start-Sleep -Seconds 1 }
    }
    throw "Backend did not become ready: see .local\dev-server.log (PID $($process.Id))."
} finally {
    Pop-Location
}
