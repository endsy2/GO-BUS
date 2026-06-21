<#
  Go Bus Express - JMeter stress test runner
  Usage:   .\run-stress.ps1 -Profile smoke
           .\run-stress.ps1 -Profile load
           .\run-stress.ps1 -Profile stress
           .\run-stress.ps1 -Profile spike
           .\run-stress.ps1 -Profile soak
           .\run-stress.ps1 -Profile booking      (race/contention test only)
  Optional overrides:
           .\run-stress.ps1 -Profile load -BaseUrl localhost -Protocol http -Port 8080
  Requires: JMeter 'bin' on PATH, or set $env:JMETER_HOME.
#>
param(
  [ValidateSet('smoke','load','stress','spike','soak','booking')]
  [string]$Profile = 'smoke',
  [string]$BaseUrl  = 'go-bus-gateway-service-production.up.railway.app',
  [string]$Protocol = 'https',
  [string]$Port     = '443',
  [string]$Email    = 'admin@gobus.com',
  [string]$Password = 'Admin123!'
)

$jmeter = if ($env:JMETER_HOME) { Join-Path $env:JMETER_HOME 'bin\jmeter.bat' } else { 'jmeter' }
$plan   = Join-Path $PSScriptRoot 'GoBus-StressTest.jmx'
$stamp  = Get-Date -Format 'yyyyMMdd-HHmmss'
$outDir = Join-Path $PSScriptRoot "results\$Profile-$stamp"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$jtl    = Join-Path $outDir 'results.jtl'
$report = Join-Path $outDir 'report'

# Per-profile read-load knobs (login/search/seatmap/busfilter). The booking race group
# is disabled in the .jmx except for the 'booking' profile note below.
$common = @(
  "-JBASE_URL=$BaseUrl", "-JPROTOCOL=$Protocol", "-JPORT=$Port",
  "-JEMAIL=$Email", "-JPASSWORD=$Password"
)

switch ($Profile) {
  'smoke'  { $p = @('-Jlogin.threads=2','-Jlogin.duration=30','-Jsearch.threads=2','-Jsearch.duration=30','-Jseatmap.threads=2','-Jseatmap.duration=30','-Jbusfilter.threads=2','-Jbusfilter.duration=30') }
  'load'   { $p = @('-Jlogin.threads=50','-Jlogin.rampup=60','-Jlogin.duration=600','-Jsearch.threads=100','-Jsearch.rampup=60','-Jsearch.duration=600','-Jseatmap.threads=80','-Jseatmap.rampup=60','-Jseatmap.duration=600','-Jbusfilter.threads=40','-Jbusfilter.rampup=60','-Jbusfilter.duration=600') }
  'stress' { $p = @('-Jlogin.threads=150','-Jlogin.rampup=120','-Jlogin.duration=600','-Jsearch.threads=250','-Jsearch.rampup=120','-Jsearch.duration=600','-Jseatmap.threads=200','-Jseatmap.rampup=120','-Jseatmap.duration=600','-Jbusfilter.threads=80','-Jbusfilter.rampup=120','-Jbusfilter.duration=600') }
  'spike'  { $p = @('-Jlogin.threads=200','-Jlogin.rampup=5','-Jlogin.duration=180','-Jsearch.threads=300','-Jsearch.rampup=5','-Jsearch.duration=180','-Jseatmap.threads=200','-Jseatmap.rampup=5','-Jseatmap.duration=180','-Jbusfilter.threads=0') }
  'soak'   { $p = @('-Jlogin.threads=30','-Jlogin.rampup=60','-Jlogin.duration=7200','-Jsearch.threads=50','-Jsearch.rampup=60','-Jsearch.duration=7200','-Jseatmap.threads=40','-Jseatmap.rampup=60','-Jseatmap.duration=7200','-Jbusfilter.threads=20','-Jbusfilter.rampup=60','-Jbusfilter.duration=7200') }
  'booking'{
    # Race/contention test: disable read groups, enable the booking group in the GUI first
    # (it ships disabled for safety) OR keep this profile and remember to flip enabled="true".
    $p = @('-Jlogin.threads=0','-Jsearch.threads=0','-Jseatmap.threads=0','-Jbusfilter.threads=0',
           '-Jbooking.threads=50','-Jbooking.rampup=0','-Jbooking.loops=1','-JscheduleId=1','-JseatIds=10, 11')
    Write-Host "NOTE: The 'Create Booking' thread group ships DISABLED in the .jmx. Open it in the JMeter GUI, enable it, save, then re-run this profile." -ForegroundColor Yellow
  }
}

Write-Host "Running profile '$Profile' against $Protocol`://$BaseUrl`:$Port" -ForegroundColor Cyan
& $jmeter -n -t $plan -l $jtl -e -o $report @common @p
Write-Host "Done. HTML report: $report\index.html" -ForegroundColor Green
