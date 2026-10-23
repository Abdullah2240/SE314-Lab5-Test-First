param(
    [switch]$SocialOnly,
    [string]$EvidencePath
)

$ErrorActionPreference = 'Stop'
$project = Split-Path -Parent $MyInvocation.MyCommand.Path
$localJdk = Join-Path $project '..\..\Lab4\jdk8\jdk8u504-b01\bin'
$javac = if (Test-Path (Join-Path $localJdk 'javac.exe')) {
    Join-Path $localJdk 'javac.exe'
} else {
    'javac'
}
$java = if (Test-Path (Join-Path $localJdk 'java.exe')) {
    Join-Path $localJdk 'java.exe'
} else {
    'java'
}
$classes = Join-Path $project 'build\classes'
New-Item -ItemType Directory -Path $classes -Force | Out-Null
$libraries = @(
    (Join-Path $project 'lib\junit-4.13.2.jar'),
    (Join-Path $project 'lib\hamcrest-core-1.3.jar'),
    (Join-Path $project 'lib\javax.json-1.0.jar')
)
$classpath = ($libraries + $classes) -join [IO.Path]::PathSeparator
$sources = @(Get-ChildItem (Join-Path $project 'src') -Filter '*.java' -Recurse -File | ForEach-Object FullName)
$sources += @(Get-ChildItem (Join-Path $project 'test') -Filter '*.java' -Recurse -File | ForEach-Object FullName)
& $javac -cp $classpath -d $classes $sources
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$tests = if ($SocialOnly) {
    @('twitter.SocialNetworkTest')
} else {
    @('twitter.ExtractTest', 'twitter.FilterTest', 'twitter.SocialNetworkTest')
}
if ($EvidencePath) {
    $destination = Join-Path $project $EvidencePath
    New-Item -ItemType Directory -Path (Split-Path -Parent $destination) -Force | Out-Null
    & $java -ea -cp $classpath org.junit.runner.JUnitCore $tests 2>&1 |
        Tee-Object -FilePath $destination
} else {
    & $java -ea -cp $classpath org.junit.runner.JUnitCore $tests
}
exit $LASTEXITCODE
