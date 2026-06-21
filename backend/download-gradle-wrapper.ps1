# PowerShell script to download Gradle wrapper jar
# This fixes the missing gradle-wrapper.jar issue

$gradleVersion = "8.5"
$wrapperDir = "gradle/wrapper"
$wrapperJar = "$wrapperDir/gradle-wrapper.jar"
$downloadUrl = "https://raw.githubusercontent.com/gradle/gradle/v$gradleVersion/gradle/wrapper/gradle-wrapper.jar"

Write-Host "Downloading Gradle Wrapper $gradleVersion..." -ForegroundColor Cyan

# Create wrapper directory if it doesn't exist
if (!(Test-Path $wrapperDir)) {
    New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null
}

# Download the wrapper jar
try {
    Write-Host "Downloading from: $downloadUrl" -ForegroundColor Yellow
    Invoke-WebRequest -Uri $downloadUrl -OutFile $wrapperJar -UseBasicParsing
    Write-Host "✓ Successfully downloaded gradle-wrapper.jar" -ForegroundColor Green
    
    # Verify the file exists and has content
    $fileInfo = Get-Item $wrapperJar
    Write-Host "✓ File size: $($fileInfo.Length) bytes" -ForegroundColor Green
    
    Write-Host "`nNow you can rebuild the Docker images:" -ForegroundColor Yellow
    Write-Host "  docker-compose -f docker-compose.dev.yml build --no-cache" -ForegroundColor White
} catch {
    Write-Host "✗ Failed to download gradle-wrapper.jar" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host "`nAlternative: Download manually from:" -ForegroundColor Yellow
    Write-Host "  https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip" -ForegroundColor White
    Write-Host "  Extract and copy gradle/wrapper/gradle-wrapper.jar to $wrapperDir/" -ForegroundColor White
    exit 1
}
