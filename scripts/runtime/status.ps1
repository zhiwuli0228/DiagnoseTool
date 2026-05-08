[CmdletBinding()]
param(
    [string]$Url
)

$ErrorActionPreference = "Stop"

$DeployRoot = $PSScriptRoot
$RuntimeDir = Join-Path $DeployRoot "runtime"
$PidFile = Join-Path $RuntimeDir "app.pid"
$SidecarPidFile = Join-Path $RuntimeDir "sidecar.pid"
$EnvFile = Join-Path $DeployRoot "app.env"

function Read-AppEnv {
    if (-not (Test-Path $EnvFile)) {
        return
    }

    Get-Content -LiteralPath $EnvFile | ForEach-Object {
        $line = $_.Trim()
        if ($line.Length -eq 0 -or $line.StartsWith("#")) {
            return
        }

        $separator = $line.IndexOf("=")
        if ($separator -le 0) {
            return
        }

        $name = $line.Substring(0, $separator).Trim()
        $value = $line.Substring($separator + 1).Trim()
        Set-Item -Path "Env:$name" -Value $value
    }
}

Read-AppEnv

$port = if ($env:APP_PORT) { [int]$env:APP_PORT } else { 8080 }
$sidecarPort = if ($env:SIDECAR_PORT) { [int]$env:SIDECAR_PORT } else { 18765 }
if (-not $Url) {
    $Url = "http://localhost:$port/"
}

function Write-ProcessStatus {
    param(
        [string]$Name,
        [string]$Path
    )

    if (Test-Path $Path) {
        $pidText = (Get-Content -LiteralPath $Path -Raw).Trim()
        $process = if ($pidText) { Get-Process -Id ([int]$pidText) -ErrorAction SilentlyContinue } else { $null }
        if ($process) {
            Write-Host "$Name process: running (PID $pidText)"
        } else {
            Write-Host "$Name process: not running (stale PID file: $pidText)"
        }
    } else {
        Write-Host "$Name process: not tracked"
    }
}

Write-ProcessStatus -Name "Application" -Path $PidFile
Write-ProcessStatus -Name "Sidecar" -Path $SidecarPidFile

try {
    $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 5
    Write-Host "HTTP: $($response.StatusCode) $Url"
} catch {
    Write-Host "HTTP: unavailable $Url"
    Write-Host $_.Exception.Message
    exit 1
}

$sidecarUrl = "http://127.0.0.1:$sidecarPort/api/sidecar/health"
try {
    $sidecarResponse = Invoke-WebRequest -Uri $sidecarUrl -UseBasicParsing -TimeoutSec 5
    Write-Host "Sidecar HTTP: $($sidecarResponse.StatusCode) $sidecarUrl"
} catch {
    Write-Host "Sidecar HTTP: unavailable $sidecarUrl"
}
