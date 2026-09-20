[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$mavenWrapper = Join-Path $projectRoot "mvnw.cmd"

if (-not (Test-Path -LiteralPath $mavenWrapper)) {
    Write-Error "Maven Wrapper was not found: $mavenWrapper"
    exit 1
}

Push-Location $projectRoot
try {
    Write-Host "Running all GarageTrack tests..." -ForegroundColor Cyan

    & $mavenWrapper clean test
    $mavenExitCode = $LASTEXITCODE

    if ($mavenExitCode -ne 0) {
        Write-Host "Tests failed. Maven exit code: $mavenExitCode" -ForegroundColor Red
        exit $mavenExitCode
    }

    Write-Host "All GarageTrack tests passed." -ForegroundColor Green
    exit 0
}
finally {
    Pop-Location
}
