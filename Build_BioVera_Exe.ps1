$ErrorActionPreference = "Stop"

$projectDir = $PSScriptRoot
$outDir = "$projectDir\out\production\pharmacie"
$resourcesDir = "$projectDir\resources"
$libDir = "$projectDir\lib"
$iconPath = "$projectDir\app_icon.ico"
$buildDir = "$projectDir\build_temp"
$outputExeDir = "$projectDir\output_exe"

Write-Host "Cleaning build directories..." -ForegroundColor Cyan
if (Test-Path $buildDir) { Remove-Item -Recurse -Force $buildDir }
if (Test-Path "$outputExeDir\BioVera Pro") { Remove-Item -Recurse -Force "$outputExeDir\BioVera Pro" }
New-Item -ItemType Directory -Force -Path $buildDir | Out-Null
New-Item -ItemType Directory -Force -Path $outputExeDir | Out-Null

Write-Host "Compiling source code..." -ForegroundColor Cyan
if (-not (Test-Path $outDir)) { New-Item -ItemType Directory -Force -Path $outDir | Out-Null }

$libs = @(
    "$libDir\mysql-connector-j-8.0.33.jar",
    "$libDir\openpdf-1.3.42.jar",
    "$libDir\lib\javafx.base.jar",
    "$libDir\lib\javafx.controls.jar",
    "$libDir\lib\javafx.fxml.jar",
    "$libDir\lib\javafx.graphics.jar"
)
$classpath = $libs -join ";"

$javaFiles = Get-ChildItem -Recurse "$projectDir\src" -Filter *.java | Select-Object -ExpandProperty FullName
$javaFilesArgs = "$projectDir\sources_list.txt"
$javaFiles | ForEach-Object { "`"$($_.Replace('\', '/'))`"" } | Out-File -Encoding ascii $javaFilesArgs

javac --release 21 -d "$outDir" -cp "$classpath" -sourcepath "$projectDir\src" "@$javaFilesArgs"

if ($LASTEXITCODE -ne 0) {
    Write-Error "Compilation failed!"
    exit 1
}
Remove-Item $javaFilesArgs

Write-Host "Creating BioVeraApp.jar..." -ForegroundColor Cyan
$jarTemp = "$projectDir\jar_temp"
if (Test-Path $jarTemp) { Remove-Item -Recurse -Force $jarTemp }
New-Item -ItemType Directory -Force -Path $jarTemp | Out-Null

& robocopy "$outDir" "$jarTemp" /E /NC /NFL /NDL /NJH /NJS | Out-Null
& robocopy "$resourcesDir" "$jarTemp" /E /NC /NFL /NDL /NJH /NJS | Out-Null
$global:LASTEXITCODE = 0

$manifestPath = "$jarTemp/MANIFEST.MF"
@"
Manifest-Version: 1.0
Main-Class: com.pharmacie.Launcher
Class-Path: .
"@ | Out-File -Encoding ascii $manifestPath

$jarPath = "$buildDir/BioVeraApp.jar"
jar cfm "$jarPath" "$manifestPath" -C "$jarTemp" .

if (-not (Test-Path $jarPath)) {
    Write-Error "JAR creation failed! File not found: $jarPath"
    exit 1
}

Write-Host "Preparing input directory..." -ForegroundColor Cyan
Copy-Item "$libDir\mysql-connector-j-8.0.33.jar" $buildDir
Copy-Item "$libDir\openpdf-1.3.42.jar" $buildDir
Copy-Item "$libDir\lib\*.jar" $buildDir

Write-Host "Running jpackage..." -ForegroundColor Cyan

$jpackageArgs = @(
    "--type", "app-image",
    "--input", $buildDir,
    "--dest", $outputExeDir,
    "--name", "BioVera Pro",
    "--main-jar", "BioVeraApp.jar",
    "--main-class", "com.pharmacie.Launcher",
    "--icon", $iconPath,
    "--vendor", "BioVera",
    "--app-version", "1.0",
    "--module-path", "$libDir\lib",
    "--add-modules", "javafx.controls,javafx.fxml,javafx.graphics,javafx.base,java.sql,java.naming"
)

& jpackage @jpackageArgs

if ($LASTEXITCODE -eq 0) {
    Write-Host "Copying JavaFX native libraries..." -ForegroundColor Cyan
    Copy-Item "$libDir\lib\*.dll" "$outputExeDir\BioVera Pro\"

    Write-Host "===========================" -ForegroundColor Green
    Write-Host " BUILD SUCCESSFULL" -ForegroundColor Green
    Write-Host "===========================" -ForegroundColor Green
    Write-Host "Executable location: $outputExeDir\BioVera Pro\BioVera Pro.exe"
} else {
    Write-Error "jpackage failed!"
}

Remove-Item -Recurse -Force $jarTemp