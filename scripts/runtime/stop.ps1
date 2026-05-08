[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"

$DeployRoot = $PSScriptRoot
$RuntimeDir = Join-Path $DeployRoot "runtime"
$PidFile = Join-Path $RuntimeDir "app.pid"
$SidecarPidFile = Join-Path $RuntimeDir "sidecar.pid"

function Stop-TrackedProcess {
    param(
        [string]$Name,
        [string]$Path
    )

    if (-not (Test-Path $Path)) {
        Write-Host "$Name is not tracked as running."
        return
    }

    $pidText = (Get-Content -LiteralPath $Path -Raw).Trim()
    if (-not $pidText) {
        Remove-Item -LiteralPath $Path -Force
        Write-Host "Empty $Name PID file removed."
        return
    }

    $process = Get-Process -Id ([int]$pidText) -ErrorAction SilentlyContinue
    if (-not $process) {
        Remove-Item -LiteralPath $Path -Force
        Write-Host "Tracked $Name process $pidText is not running. PID file removed."
        return
    }

    Stop-Process -Id $process.Id -Force
    Remove-Item -LiteralPath $Path -Force
    Write-Host "$Name stopped. PID: $($process.Id)"
}

Stop-TrackedProcess -Name "Application" -Path $PidFile
Stop-TrackedProcess -Name "Sidecar" -Path $SidecarPidFile
