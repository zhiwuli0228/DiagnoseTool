[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"

$DeployRoot = $PSScriptRoot
$RuntimeDir = Join-Path $DeployRoot "runtime"
$PidFile = Join-Path $RuntimeDir "app.pid"

if (-not (Test-Path $PidFile)) {
    Write-Host "No PID file found. Application is not tracked as running."
    exit 0
}

$pidText = (Get-Content -LiteralPath $PidFile -Raw).Trim()
if (-not $pidText) {
    Remove-Item -LiteralPath $PidFile -Force
    Write-Host "Empty PID file removed."
    exit 0
}

$process = Get-Process -Id ([int]$pidText) -ErrorAction SilentlyContinue
if (-not $process) {
    Remove-Item -LiteralPath $PidFile -Force
    Write-Host "Tracked process $pidText is not running. PID file removed."
    exit 0
}

Stop-Process -Id $process.Id -Force
Remove-Item -LiteralPath $PidFile -Force
Write-Host "Application stopped. PID: $($process.Id)"
