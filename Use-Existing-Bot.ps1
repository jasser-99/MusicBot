param([string]$ExpectedApplicationId)
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
Write-Host 'Paste the selected Discord BOT token below. Input is hidden.'
$secureToken = Read-Host 'Bot token' -AsSecureString
$credential = New-Object System.Net.NetworkCredential('', $secureToken)
$botToken = $credential.Password
try {
    $headers = @{ Authorization = "Bot $botToken" }
    $botIdentity = Invoke-RestMethod -Uri 'https://discord.com/api/v10/users/@me' -Headers $headers -TimeoutSec 20
    if ($ExpectedApplicationId -and $botIdentity.id -ne $ExpectedApplicationId) {
        throw 'This token belongs to a different application.'
    }
    if (-not $botIdentity.bot) { throw 'A bot token is required.' }
    $ownerId = $null
    try {
        $application = Invoke-RestMethod -Uri 'https://discord.com/api/v10/applications/@me' -Headers $headers -TimeoutSec 20
        if ($application.team.owner_user_id) { $ownerId = $application.team.owner_user_id }
        elseif ($application.owner.id) { $ownerId = $application.owner.id }
    } catch { Write-Host 'Owner lookup unavailable.' }
    if (-not $ownerId) { $ownerId = Read-Host 'Your Discord user ID' }
    if ($ownerId -notmatch '^\d{17,20}$') { throw 'A valid owner user ID is required.' }
    if (Test-Path -LiteralPath 'config.txt') { $configText = Get-Content -LiteralPath 'config.txt' -Raw }
    else { $configText = Get-Content -LiteralPath 'src/main/resources/reference.conf' -Raw }
    $tokenJson = ConvertTo-Json -InputObject $botToken -Compress
    $configText = [regex]::Replace($configText, '(?m)^\s*token\s*=.*$', ('  token = ' + $tokenJson))
    $configText = [regex]::Replace($configText, '(?m)^\s*owner\s*=.*$', ('  owner = ' + $ownerId))
    $configPath = Join-Path $PSScriptRoot 'config.txt'
    [IO.File]::WriteAllText($configPath, $configText, (New-Object Text.UTF8Encoding($false)))
    $configAcl = Get-Acl -LiteralPath $configPath
    $configAcl.SetAccessRuleProtection($true, $false)
    $userIdentity = [Security.Principal.WindowsIdentity]::GetCurrent().Name
    $configAcl.SetAccessRule((New-Object Security.AccessControl.FileSystemAccessRule($userIdentity, 'FullControl', 'Allow')))
    Set-Acl -LiteralPath $configPath -AclObject $configAcl
    Write-Host ('Configured bot: ' + $botIdentity.username + '. Token value omitted.')
} catch {
    Write-Host 'Bot setup could not finish. Check the bot token, application selection, owner ID, and internet connection.'
    exit 1
} finally {
    $headers = $null
    $botToken = $null
    $tokenJson = $null
    $configText = $null
    $credential = $null
}
