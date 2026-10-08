$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
if (Test-Path -LiteralPath 'config.txt') {
    Write-Host 'config.txt already exists. Edit it locally if you need to change settings.'
    exit 0
}
Write-Host 'Enter your Discord BOT token locally. Input is hidden and never printed.'
$secureToken = Read-Host 'Bot token' -AsSecureString
$credential = New-Object System.Net.NetworkCredential('', $secureToken)
$botToken = $credential.Password
if ([string]::IsNullOrWhiteSpace($botToken) -or $botToken -match '\s') { throw 'Invalid token format.' }
$ownerId = Read-Host 'Your Discord user ID (Developer Mode > Copy User ID)'
if ($ownerId -notmatch '^\d{17,20}$') { throw 'Enter a valid Discord user ID.' }
$musicFolder = Read-Host 'Music folder (press Enter for the Music subfolder)'
if ([string]::IsNullOrWhiteSpace($musicFolder)) { $musicFolder = 'Music' }
New-Item -ItemType Directory -Force -Path $musicFolder | Out-Null
$configText = Get-Content -LiteralPath 'src/main/resources/reference.conf' -Raw
$tokenJson = ConvertTo-Json -InputObject $botToken -Compress
$folderJson = ConvertTo-Json -InputObject $musicFolder -Compress
$configText = $configText.Replace('"BOT_TOKEN_HERE"', $tokenJson).Replace('owner = 0 # OWNER ID', "owner = $ownerId # OWNER ID").Replace('localMusicFolder = "Music"', "localMusicFolder = $folderJson")
[IO.File]::WriteAllText((Join-Path $PSScriptRoot 'config.txt'), $configText, (New-Object Text.UTF8Encoding($false)))
$configAcl = Get-Acl -LiteralPath 'config.txt'
$configAcl.SetAccessRuleProtection($true, $false)
$userIdentity = [Security.Principal.WindowsIdentity]::GetCurrent().Name
$accessRule = New-Object Security.AccessControl.FileSystemAccessRule($userIdentity, 'FullControl', 'Allow')
$configAcl.SetAccessRule($accessRule)
Set-Acl -LiteralPath 'config.txt' -AclObject $configAcl
$botToken = $null
$credential = $null
Write-Host 'Saved local config. Enable Message Content Intent in the Developer Portal.'
Write-Host 'Invite the application with bot and applications.commands scopes.'
Write-Host 'Permissions: View Channels, Send Messages, Read Message History, Embed Links, Connect, Speak.'
Write-Host 'Then run Start Bot.bat. In Discord, use /settc in your music text channel.'
