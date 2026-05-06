[CmdletBinding()]
param(
    [switch]$SkipTests,
    [switch]$SkipNpmInstall
)

$ErrorActionPreference = "Stop"

$RepoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$FrontendDir = Join-Path $RepoRoot "frontend"
$BackendJarPattern = Join-Path $RepoRoot "target\*.jar"
$FrontendDist = Join-Path $FrontendDir "dist"

function Require-Command {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name,
        [Parameter(Mandatory = $true)]
        [string]$InstallHint
    )

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Missing prerequisite '$Name'. $InstallHint"
    }
}

function Invoke-Step {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name,
        [Parameter(Mandatory = $true)]
        [scriptblock]$Action
    )

    Write-Host ""
    Write-Host "==> $Name"
    & $Action
}

Require-Command "java" "Install Java 21 and make sure java is available on PATH."
Require-Command "mvn" "Install Maven and make sure mvn is available on PATH."
Require-Command "node" "Install Node.js and make sure node is available on PATH."
Require-Command "npm" "Install npm and make sure npm is available on PATH."

Invoke-Step "Backend build" {
    Push-Location $RepoRoot
    try {
        if ($SkipTests) {
            & mvn "-DskipTests" package
        } else {
            & mvn package
        }

        if ($LASTEXITCODE -ne 0) {
            throw "Backend Maven build failed with exit code $LASTEXITCODE."
        }
    } finally {
        Pop-Location
    }
}

Invoke-Step "Frontend dependencies" {
    Push-Location $FrontendDir
    try {
        if (-not $SkipNpmInstall) {
            if (Test-Path (Join-Path $FrontendDir "package-lock.json")) {
                & npm ci
            } else {
                & npm install
            }

            if ($LASTEXITCODE -ne 0) {
                throw "Frontend dependency installation failed with exit code $LASTEXITCODE."
            }
        } else {
            Write-Host "Skipping npm install because -SkipNpmInstall was provided."
        }
    } finally {
        Pop-Location
    }
}

Invoke-Step "Frontend build" {
    Push-Location $FrontendDir
    try {
        & npm run build
        if ($LASTEXITCODE -ne 0) {
            throw "Frontend build failed with exit code $LASTEXITCODE."
        }
    } finally {
        Pop-Location
    }
}

$BackendJar = Get-ChildItem -Path $BackendJarPattern -File |
    Where-Object { $_.Name -notlike "*.original" } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $BackendJar) {
    throw "Backend build completed but no jar was found under target/."
}

if (-not (Test-Path (Join-Path $FrontendDist "index.html"))) {
    throw "Frontend build completed but frontend/dist/index.html was not found."
}

Write-Host ""
Write-Host "Build complete."
Write-Host "Backend artifact: $($BackendJar.FullName)"
Write-Host "Frontend assets:  $FrontendDist"
