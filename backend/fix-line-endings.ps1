# PowerShell script to fix line endings for gradlew
# Converts Windows line endings (CRLF) to Unix line endings (LF)

Write-Host "Converting gradlew to Unix line endings..." -ForegroundColor Cyan

$gradlewPath = "gradlew"

if (Test-Path $gradlewPath) {
    # Read the file content
    $content = Get-Content $gradlewPath -Raw
    
    # Replace CRLF with LF
    $content = $content -replace "`r`n", "`n"
    
    # Write back without BOM
    $utf8NoBom = New-Object System.Text.UTF8Encoding $false
    [System.IO.File]::WriteAllText((Resolve-Path $gradlewPath), $content, $utf8NoBom)
    
    Write-Host "✓ Line endings fixed for gradlew" -ForegroundColor Green
} else {
    Write-Host "✗ gradlew file not found!" -ForegroundColor Red
    exit 1
}

Write-Host "`nNow rebuild the Docker images:" -ForegroundColor Yellow
Write-Host "  docker-compose -f docker-compose.dev.yml build --no-cache" -ForegroundColor White
