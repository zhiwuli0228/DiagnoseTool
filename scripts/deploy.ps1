[CmdletBinding()]
param(
    [string]$DeployDir,
    [string]$BackendJar,
    [string]$FrontendDist,
    [switch]$SkipBuild,
    [switch]$SkipTests,
    [switch]$SkipNpmInstall
)

$ErrorActionPreference = "Stop"

$RepoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
if ([string]::IsNullOrWhiteSpace($FrontendDist)) {
    $FrontendDist = Join-Path $RepoRoot "frontend\dist"
}
$RuntimeScriptsDir = Join-Path $PSScriptRoot "runtime"
if ([string]::IsNullOrWhiteSpace($DeployDir)) {
    $DeployDir = Join-Path $RepoRoot "deploy"
}
$DeployPath = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($DeployDir)
$TempDeployPath = "$DeployPath.tmp"

function Require-Path {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,
        [Parameter(Mandatory = $true)]
        [string]$Message
    )

    if (-not (Test-Path $Path)) {
        throw $Message
    }
}

if (-not $SkipBuild) {
    $buildArgs = @()
    if ($SkipTests) { $buildArgs += "-SkipTests" }
    if ($SkipNpmInstall) { $buildArgs += "-SkipNpmInstall" }
    & (Join-Path $PSScriptRoot "build.ps1") @buildArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Build script failed with exit code $LASTEXITCODE."
    }
}

if ([string]::IsNullOrWhiteSpace($BackendJar)) {
    $BackendJarFile = Get-ChildItem -Path (Join-Path $RepoRoot "target\*.jar") -File |
        Where-Object { $_.Name -notlike "*.original" } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
} else {
    $BackendJarFile = Get-Item -LiteralPath $BackendJar -ErrorAction SilentlyContinue
}

if (-not $BackendJarFile) {
    throw "No backend jar found. Run scripts/build.ps1 first, omit -SkipBuild, or pass -BackendJar <path>."
}

Require-Path (Join-Path $FrontendDist "index.html") "No frontend build found. Run scripts/build.ps1 first, omit -SkipBuild, or pass -FrontendDist <path>."

if (Test-Path $TempDeployPath) {
    Remove-Item -LiteralPath $TempDeployPath -Recurse -Force
}

New-Item -ItemType Directory -Force -Path $TempDeployPath | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $TempDeployPath "app") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $TempDeployPath "frontend") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $TempDeployPath "logs") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $TempDeployPath "runtime") | Out-Null

Copy-Item -LiteralPath $BackendJarFile.FullName -Destination (Join-Path $TempDeployPath "app\thread-doctor.jar") -Force
Copy-Item -Path (Join-Path $FrontendDist "*") -Destination (Join-Path $TempDeployPath "frontend") -Recurse -Force
Copy-Item -Path (Join-Path $RuntimeScriptsDir "*") -Destination $TempDeployPath -Recurse -Force

if (Test-Path $DeployPath) {
    Remove-Item -LiteralPath $DeployPath -Recurse -Force
}

Move-Item -LiteralPath $TempDeployPath -Destination $DeployPath

Write-Host ""
Write-Host "Deployment package ready: $DeployPath"
Write-Host "Start:  powershell -ExecutionPolicy Bypass -File `"$DeployPath\start.ps1`""
Write-Host "Status: powershell -ExecutionPolicy Bypass -File `"$DeployPath\status.ps1`""
Write-Host "Stop:   powershell -ExecutionPolicy Bypass -File `"$DeployPath\stop.ps1`""
