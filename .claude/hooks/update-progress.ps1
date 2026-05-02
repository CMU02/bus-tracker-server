$ErrorActionPreference = "SilentlyContinue"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$rawInput = [Console]::In.ReadToEnd()
if ([string]::IsNullOrWhiteSpace($rawInput)) {
    exit 0
}

try {
    $payload = $rawInput | ConvertFrom-Json
} catch {
    exit 0
}

$progressPath = ".agents/rules/project-progress.md"
if (-not (Test-Path -LiteralPath $progressPath)) {
    exit 0
}

function Set-ProgressChecked {
    param(
        [Parameter(Mandatory = $true)][string]$Content,
        [Parameter(Mandatory = $true)][string]$Marker
    )

    $pattern = "- \[ \] (?<label>.+?) <!-- $([regex]::Escape($Marker)) -->"
    return [regex]::Replace($Content, $pattern, "- [x] `${label} <!-- $Marker -->")
}

$toolName = [string]$payload.tool_name
$command = [string]$payload.tool_input.command
$isSuccessfulGradleTest = $toolName -eq "Bash" -and $command -match "(^|\s|[\\/])gradlew(\.bat)?(\s|$).*?\btest\b"

if ($isSuccessfulGradleTest) {
    $content = Get-Content -LiteralPath $progressPath -Raw
    $content = Set-ProgressChecked $content "progress:auto:implementation-complete"
    $content = Set-ProgressChecked $content "progress:auto:tests-complete"
    Set-Content -LiteralPath $progressPath -Value $content -Encoding UTF8
}

exit 0
