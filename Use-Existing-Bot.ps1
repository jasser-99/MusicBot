param([string]$ExpectedApplicationId)
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
Write-Host 'Paste the selected Discord BOT token below. Input is hidden.'
$secureToken = Read-Host 'Bot token' -AsSecureString
$credential = New-Object System.Net.NetworkCredential('', $secureToken)
$botToken = $credential.Password.Trim()
$botToken = $botToken -replace '^Bot\s+', ''
$setupStage = 'token verification'
$safeFailure = $null
try {
    if (-not $botToken) { $safeFailure = 'No token was entered.'; throw 'Invalid input' }
    $headers = @{ Authorization = "Bot $botToken" }
    $botIdentity = Invoke-RestMethod -Uri 'https://discord.com/api/v10/users/@me' -Headers $headers -TimeoutSec 20
    if ($ExpectedApplicationId -and $botIdentity.id -ne $ExpectedApplicationId) {
        $safeFailure = 'This token belongs to another bot. Use the token from Creamer Pawg (application 1204361957394882652).'
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
    if ($ownerId -notmatch '^\d{17,20}$') { $safeFailure = 'The owner ID must be your numeric Discord user ID (17 to 20 digits).'; throw 'Invalid owner' }
    $setupStage = 'saving configuration'
    if (Test-Path -LiteralPath 'config.txt') { $configText = Get-Content -LiteralPath 'config.txt' -Raw }
    else { $configText = Get-Content -LiteralPath 'src/main/resources/reference.conf' -Raw }
    $tokenJson = ConvertTo-Json -InputObject $botToken -Compress
    $configText = [regex]::Replace($configText, '(?m)^\s*token\s*=.*$', ('  token = ' + $tokenJson))
    $configText = [regex]::Replace($configText, '(?m)^\s*owner\s*=.*$', ('  owner = ' + $ownerId))
    $configPath = Join-Path $PSScriptRoot 'config.txt'
    [IO.File]::WriteAllText($configPath, $configText, (New-Object Text.UTF8Encoding($false)))
    $setupStage = 'protecting configuration'
    $userIdentity = [Security.Principal.WindowsIdentity]::GetCurrent().Name
    & icacls.exe $configPath /inheritance:r /grant:r ($userIdentity + ':(F)') | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Configuration permission update failed' }
    Write-Host ('Configured bot: ' + $botIdentity.username + '. Token value omitted.')
} catch {
    $httpStatus = $null
    if ($_.Exception.Response) { $httpStatus = [int]$_.Exception.Response.StatusCode }
    if ($safeFailure) { Write-Host $safeFailure }
    elseif ($httpStatus -eq 401) { Write-Host 'Discord rejected the token (401). Copy the current BOT token from Creamer Pawg and try again.' }
    elseif ($httpStatus -eq 403) { Write-Host 'Discord denied token verification (403). Confirm you copied a BOT token from the Bot settings page.' }
    elseif ($httpStatus -eq 429) { Write-Host 'Discord rate limited verification (429). Wait a moment before retrying.' }
    elseif ($httpStatus) { Write-Host ('Discord returned HTTP ' + $httpStatus + ' during ' + $setupStage + '.') }
    else { Write-Host ('Setup failed during ' + $setupStage + '. Error type: ' + $_.Exception.GetType().Name + '. Token value omitted.') }
    exit 1
} finally {
    $headers = $null
    $botToken = $null
    $tokenJson = $null
    $configText = $null
    $credential = $null
}
