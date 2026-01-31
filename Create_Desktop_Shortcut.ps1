$ErrorActionPreference = "Stop"

$WshShell = New-Object -ComObject WScript.Shell
$desktopPath = [System.Environment]::GetFolderPath("Desktop")
$shortcutPath = "$desktopPath\BioVera Pro.lnk"

$projectDir = "$PSScriptRoot"
$exePath = "$projectDir\output_exe\BioVera Pro\BioVera Pro.exe"
$iconPath = "$projectDir\app_icon.ico"

if (Test-Path $shortcutPath) {
    Remove-Item $shortcutPath -Force
}

$oldShortcut = "$desktopPath\BioVera Pro v1.lnk"
if (Test-Path $oldShortcut) {
    Remove-Item $oldShortcut -Force
}

$shortcut = $WshShell.CreateShortcut($shortcutPath)
$shortcut.TargetPath = $exePath
$shortcut.WorkingDirectory = "$projectDir\output_exe\BioVera Pro"
$shortcut.WindowStyle = 1
$shortcut.IconLocation = $iconPath
$shortcut.Description = "BioVera Pro - Système de Gestion de Pharmacie"
$shortcut.Save()

Write-Host "==========================================" -ForegroundColor Green
Write-Host "   RACCOURCI CREE SUR LE BUREAU !" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host "Raccourci: $shortcutPath"
Write-Host "Cible: $exePath"
Write-Host "Icône: $iconPath"