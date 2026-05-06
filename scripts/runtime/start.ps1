[CmdletBinding()]
param(
    [int]$Port,
    [string]$JavaOpts,
    [string]$SpringProfilesActive,
    [string]$AppArgs
)

$ErrorActionPreference = "Stop"

$DeployRoot = $PSScriptRoot
$JarPath = Join-Path $DeployRoot "app\thread-doctor.jar"
$FrontendPath = Join-Path $DeployRoot "frontend"
$RuntimeDir = Join-Path $DeployRoot "runtime"
$LogsDir = Join-Path $DeployRoot "logs"
$PidFile = Join-Path $RuntimeDir "app.pid"
$EnvFile = Join-Path $DeployRoot "app.env"
$StdOutLog = Join-Path $LogsDir "app.out.log"
$StdErrLog = Join-Path $LogsDir "app.err.log"

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

function Split-Args {
    param([string]$Value)

    if ([string]::IsNullOrWhiteSpace($Value)) {
        return @()
    }

    return $Value -split "\s+"
}

function Quote-Argument {
    param([string]$Value)

    if ($null -eq $Value) {
        return '""'
    }

    if ($Value -notmatch '[\s"]') {
        return $Value
    }

    return '"' + ($Value -replace '\\(?=")', '\' -replace '"', '\"') + '"'
}

if (-not (Get-Command "java" -ErrorAction SilentlyContinue)) {
    throw "Missing prerequisite 'java'. Install Java 21 and make sure java is available on PATH."
}

if (-not (Test-Path $JarPath)) {
    throw "Backend jar not found at $JarPath. Run scripts/deploy.ps1 first."
}

if (-not (Test-Path (Join-Path $FrontendPath "index.html"))) {
    throw "Frontend assets not found at $FrontendPath. Run scripts/deploy.ps1 first."
}

New-Item -ItemType Directory -Force -Path $RuntimeDir | Out-Null
New-Item -ItemType Directory -Force -Path $LogsDir | Out-Null

Read-AppEnv

if (-not $PSBoundParameters.ContainsKey("Port")) {
    $Port = if ($env:APP_PORT) { [int]$env:APP_PORT } else { 8080 }
}
if (-not $PSBoundParameters.ContainsKey("JavaOpts")) {
    $JavaOpts = $env:JAVA_OPTS
}
if (-not $PSBoundParameters.ContainsKey("SpringProfilesActive")) {
    $SpringProfilesActive = $env:SPRING_PROFILES_ACTIVE
}
if (-not $PSBoundParameters.ContainsKey("AppArgs")) {
    $AppArgs = $env:APP_ARGS
}

if (Test-Path $PidFile) {
    $existingPid = (Get-Content -LiteralPath $PidFile -Raw).Trim()
    if ($existingPid -and (Get-Process -Id ([int]$existingPid) -ErrorAction SilentlyContinue)) {
        throw "Application already appears to be running with PID $existingPid. Run stop.ps1 first."
    }
    Remove-Item -LiteralPath $PidFile -Force
}

$staticLocation = "file:///$($FrontendPath.Replace('\', '/'))/"
$arguments = @()
$arguments += Split-Args $JavaOpts
$arguments += @("-jar", $JarPath)
$arguments += "--server.port=$Port"
$arguments += "--spring.web.resources.static-locations=$staticLocation"
if (-not [string]::IsNullOrWhiteSpace($SpringProfilesActive)) {
    $arguments += "--spring.profiles.active=$SpringProfilesActive"
}
$arguments += Split-Args $AppArgs

$argumentLine = ($arguments | ForEach-Object { Quote-Argument $_ }) -join " "
$process = Start-Process -FilePath "java" `
    -ArgumentList $argumentLine `
    -WorkingDirectory $DeployRoot `
    -RedirectStandardOutput $StdOutLog `
    -RedirectStandardError $StdErrLog `
    -WindowStyle Hidden `
    -PassThru

$process.Id | Set-Content -LiteralPath $PidFile -Encoding ASCII

Write-Host "Application started."
Write-Host "PID: $($process.Id)"
Write-Host "URL: http://localhost:$Port/"
Write-Host "Logs: $LogsDir"
