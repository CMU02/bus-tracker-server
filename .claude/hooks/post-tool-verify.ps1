$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$rawInput = [Console]::In.ReadToEnd()
if ([string]::IsNullOrWhiteSpace($rawInput)) {
    exit 0
}

try {
    $payload = $rawInput | ConvertFrom-Json
} catch {
    Write-Error "PostToolUse verification failed: hook input JSON could not be parsed."
    exit 2
}

$filePath = $payload.tool_input.file_path
if ([string]::IsNullOrWhiteSpace($filePath)) {
    $filePath = $payload.tool_input.path
}

if ([string]::IsNullOrWhiteSpace($filePath) -or -not (Test-Path -LiteralPath $filePath -PathType Leaf)) {
    exit 0
}

$extension = [System.IO.Path]::GetExtension($filePath).ToLowerInvariant()
$checkedExtensions = @(".java", ".gradle", ".yaml", ".yml", ".json", ".md", ".properties", ".ps1", ".sh")
if ($checkedExtensions -notcontains $extension) {
    exit 0
}

$lines = Get-Content -LiteralPath $filePath
$issues = New-Object System.Collections.Generic.List[string]

for ($i = 0; $i -lt $lines.Count; $i++) {
    if ($lines[$i] -match "\s+$") {
        $issues.Add("line $($i + 1): trailing whitespace")
    }
}

$content = Get-Content -LiteralPath $filePath -Raw
if ($content -match "(?i)(api[_-]?key|secret|password|token)\s*=\s*['""][^'""]+['""]") {
    $issues.Add("possible hard-coded secret assignment")
}

if ($issues.Count -gt 0) {
    $message = "PostToolUse verification failed for ${filePath}:`n- " + ($issues -join "`n- ")
    Write-Error $message
    exit 2
}

exit 0
