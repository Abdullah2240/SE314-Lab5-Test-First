param(
    [string]$SampleUrl,
    [string]$SampleFile,
    [string]$EvidencePath
)

$ErrorActionPreference = 'Continue'
$project = Split-Path -Parent $MyInvocation.MyCommand.Path
$localJava = Join-Path $project '..\..\Lab4\jdk8\jdk8u504-b01\bin\java.exe'
$java = if (Test-Path $localJava) { $localJava } else { 'java' }
$classpath = @(
    (Join-Path $project 'build\classes'),
    (Join-Path $project 'lib\javax.json-1.0.jar')
) -join [IO.Path]::PathSeparator
$mainArgs = @('-ea', '-cp', $classpath, 'twitter.Main')
if ($SampleFile) {
    $SampleUrl = ([Uri](Resolve-Path $SampleFile).Path).AbsoluteUri
}
if ($SampleUrl) { $mainArgs += $SampleUrl }

if ($EvidencePath) {
    $destination = Join-Path $project $EvidencePath
    New-Item -ItemType Directory -Path (Split-Path -Parent $destination) -Force | Out-Null
    & $java $mainArgs 2>&1 | ForEach-Object { $_.ToString() } |
        Tee-Object -FilePath $destination
} else {
    & $java $mainArgs
}
exit $LASTEXITCODE
