# Load .env and start the Spring Boot application
$envFile = Join-Path $PSScriptRoot ".env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
            [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process')
        }
    }
    Write-Host ".env loaded" -ForegroundColor Green
} else {
    Write-Warning ".env file not found, using application.properties defaults"
}

& "$PSScriptRoot\mvnw.cmd" spring-boot:run
