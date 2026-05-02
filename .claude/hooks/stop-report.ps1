$ErrorActionPreference = "SilentlyContinue"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$status = git status --short 2>$null
if (-not [string]::IsNullOrWhiteSpace($status)) {
    Write-Output "Guardrail summary: working tree has changes. Report the completed task and mention verification status."
}

exit 0
