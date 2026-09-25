Set-Location $PSScriptRoot

$modules = 'parent', 'starter'

$services = @(
    [PSCustomObject]@{ Name = 'authservice'; Port = 8010 },
    [PSCustomObject]@{ Name = 'configurationservice'; Port = 8015 }
)

function Disable-QuickEdit {
    $consoleRoot = 'HKCU:\Console'
    New-Item -Path $consoleRoot -Force | Out-Null
    @($consoleRoot) + (Get-ChildItem -Path $consoleRoot -ErrorAction SilentlyContinue | Select-Object -ExpandProperty PSPath) | ForEach-Object { New-ItemProperty -Path $_ -Name QuickEdit -PropertyType DWord -Value 0 -Force | Out-Null }
}

function Write-Section([string]$Title) {
    $separator = '=' * ($Host.UI.RawUI.WindowSize.Width - 1)
    Write-Host "$separator`n $($Title.ToUpper()) `n$separator"
}

function Invoke-Step([string]$Title, [scriptblock]$Action) {
    Write-Section $Title; $global:LASTEXITCODE = 0; & $Action; if ($LASTEXITCODE) { exit 1 }
}

function Stop-Ports {
    Get-NetTCPConnection -LocalPort @($services.Port) -State Listen -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
}

function Wait-Port([int]$Port) {
    while (-not (Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue)) { Start-Sleep -Seconds 1 }
}

function Start-Docker {
    Invoke-Step 'Docker' { docker compose -f .docker/docker-compose.yaml up --detach --remove-orphans }
}

function Build-Modules {
    foreach ($module in $modules) {
        Invoke-Step $module { mvn -f $module clean install -DskipTests }
    }
}

function Start-Services {
    foreach ($service in $services) {
        Invoke-Step $service.Name { Start-Process pwsh "-Command mvn -f $($service.Name) spring-boot:run -DskipTests" -NoNewWindow; Wait-Port $service.Port }
    }
}

Disable-QuickEdit
Stop-Ports
Start-Docker
Build-Modules
Start-Services
Write-Section 'Ready'
Read-Host
