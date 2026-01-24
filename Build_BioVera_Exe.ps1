# Build Script for BioVera Pro
# This script bundles the application into a JAR and then creates an EXE with the custom icon.

$ErrorActionPreference = "Stop"

$projectDir = $PSScriptRoot
$outDir = "$projectDir\out\production\pharmacie"
$resourcesDir = "$projectDir\resources"
$libDir = "$projectDir\lib"
$iconPath = "$projectDir\app_icon.ico"
$buildDir = "$projectDir\build_temp"
$outputExeDir = "$projectDir\output_exe"

# 1. Clean and Setup Build Directory
Write-Host "Cleaning build directories..." -ForegroundColor Cyan
if (Test-Path $buildDir) { Remove-Item -Recurse -Force $buildDir }
if (Test-Path "$outputExeDir\BioVera Pro") { Remove-Item -Recurse -Force "$outputExeDir\BioVera Pro" }
New-Item -ItemType Directory -Force -Path $buildDir | Out-Null
New-Item -ItemType Directory -Force -Path $outputExeDir | Out-Null

# 1.5. Compile Source Code
Write-Host "Compiling source code..." -ForegroundColor Cyan
if (-not (Test-Path $outDir)) { New-Item -ItemType Directory -Force -Path $outDir | Out-Null }

# Build Classpath
$libs = @(
    "$libDir\mysql-connector-j-8.0.33.jar",
    "$libDir\openpdf-1.3.42.jar",
    "$libDir\lib\javafx.base.jar",
    "$libDir\lib\javafx.controls.jar",
    "$libDir\lib\javafx.fxml.jar",
    "$libDir\lib\javafx.graphics.jar"
)
$classpath = $libs -join ";"

# Get all java files
$javaFiles = Get-ChildItem -Recurse "$projectDir\src" -Filter *.java | Select-Object -ExpandProperty FullName
$javaFilesArgs = "$projectDir\sources_list.txt"
# Convert to forward slashes and quote to handle spaces and javac parsing complexities on some systems
$javaFiles | ForEach-Object { "`"$($_.Replace('\', '/'))`"" } | Out-File -Encoding ascii $javaFilesArgs

# Compile
# Use --% in PowerShell to prevent it from parsing quotes too aggressively for legacy commands, 
# or ensure args are passed correctly. simpler to just rely on correct string expansion but verify quotes.
javac --release 21 -d "$outDir" -cp "$classpath" -sourcepath "$projectDir\src" "@$javaFilesArgs"

if ($LASTEXITCODE -ne 0) {
    Write-Error "Compilation failed!"
    exit 1
}
Remove-Item $javaFilesArgs

# 2. Create BioVeraApp.jar
Write-Host "Creating BioVeraApp.jar..." -ForegroundColor Cyan
# Copy classes and resources to a temp folder for jarring
$jarTemp = "$projectDir\jar_temp"
if (Test-Path $jarTemp) { Remove-Item -Recurse -Force $jarTemp }
New-Item -ItemType Directory -Force -Path $jarTemp | Out-Null

# Copy compiled classes and resources using robocopy for robust directory merging
& robocopy "$outDir" "$jarTemp" /E /NC /NFL /NDL /NJH /NJS | Out-Null
& robocopy "$resourcesDir" "$jarTemp" /E /NC /NFL /NDL /NJH /NJS | Out-Null
# Reset exit code because robocopy uses non-zero for success
$global:LASTEXITCODE = 0

# Create Manifest
$manifestPath = "$jarTemp/MANIFEST.MF"
@"
Manifest-Version: 1.0
Main-Class: com.pharmacie.Launcher
Class-Path: .
"@ | Out-File -Encoding ascii $manifestPath

# Create JAR
$jarPath = "$buildDir/BioVeraApp.jar"
jar cfm "$jarPath" "$manifestPath" -C "$jarTemp" .

if (-not (Test-Path $jarPath)) {
    Write-Error "JAR creation failed! File not found: $jarPath"
    exit 1
}

# 3. Prepare Input Directory for jpackage
Write-Host "Preparing input directory..." -ForegroundColor Cyan
# Copy dependencies
Copy-Item "$libDir\mysql-connector-j-8.0.33.jar" $buildDir
Copy-Item "$libDir\openpdf-1.3.42.jar" $buildDir
# Note: JavaFX jars are usually in lib\lib, typical jpackage usage involves referencing them in module-path
# But if we want to bundle them as jars, we can copy them too. 
# However, standard practice with modules: use --module-path for javafx.
# We will use the non-modular jar approach where we put everything in input if possible, 
# OR use --module-path for FX and classpath for the app.
# Let's stick to the cfg structure we saw: $APPDIR\javafx-swt.jar etc.
# This means the previous build likely copied FX jars to the input folder.
Copy-Item "$libDir\lib\*.jar" $buildDir

# 4. Run jpackage
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

# Execute jpackage
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

# Cleanup temp
Remove-Item -Recurse -Force $jarTemp
# Remove-Item -Recurse -Force $buildDir # Keep for debug if needed
