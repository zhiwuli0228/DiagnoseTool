[CmdletBinding()]
param(
    [string]$Url
)

$ErrorActionPreference = "Stop"

$DeployRoot = $PSScriptRoot
$RuntimeDir = Join-Path $DeployRoot "runtime"
$PidFile = Join-Path $RuntimeDir "app.pid"
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
if (-not $Url) {
    $Url = "http://localhost:$port/"
}

if (Test-Path $PidFile) {
    $pidText = (Get-Content -LiteralPath $PidFile -Raw).Trim()
    $process = if ($pidText) { Get-Process -Id ([int]$pidText) -ErrorAction SilentlyContinue } else { $null }
    if ($process) {
        Write-Host "Process: running (PID $pidText)"
    } else {
        Write-Host "Process: not running (stale PID file: $pidText)"
    }
} else {
    Write-Host "Process: not tracked"
}

try {
    $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 5
    Write-Host "HTTP: $($response.StatusCode) $Url"
} catch {
    Write-Host "HTTP: unavailable $Url"
    Write-Host $_.Exception.Message
    exit 1
}
