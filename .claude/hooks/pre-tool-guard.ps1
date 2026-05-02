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
    Write-Error "Guardrail failed: hook input JSON could not be parsed."
    exit 2
}

$projectRoot = if ($env:CLAUDE_PROJECT_DIR) { $env:CLAUDE_PROJECT_DIR } else { $payload.cwd }
if ([string]::IsNullOrWhiteSpace($projectRoot)) {
    $projectRoot = (Get-Location).Path
}

$rootFull = [System.IO.Path]::GetFullPath($projectRoot).TrimEnd([System.IO.Path]::DirectorySeparatorChar, [System.IO.Path]::AltDirectorySeparatorChar)

function Write-Decision {
    param(
        [Parameter(Mandatory = $true)][string]$Decision,
        [Parameter(Mandatory = $true)][string]$Reason
    )

    @{
        hookSpecificOutput = @{
            hookEventName = "PreToolUse"
            permissionDecision = $Decision
            permissionDecisionReason = $Reason
        }
    } | ConvertTo-Json -Depth 10 -Compress
    exit 0
}

function Resolve-ProjectPath {
    param([string]$Path)

    if ([string]::IsNullOrWhiteSpace($Path)) {
        return $null
    }

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return [System.IO.Path]::GetFullPath($Path)
    }

    return [System.IO.Path]::GetFullPath((Join-Path $rootFull $Path))
}

function Test-InProject {
    param([string]$Path)

    $full = Resolve-ProjectPath $Path
    if ($null -eq $full) {
        return $false
    }

    return $full.Equals($rootFull, [System.StringComparison]::OrdinalIgnoreCase) -or
        $full.StartsWith($rootFull + [System.IO.Path]::DirectorySeparatorChar, [System.StringComparison]::OrdinalIgnoreCase) -or
        $full.StartsWith($rootFull + [System.IO.Path]::AltDirectorySeparatorChar, [System.StringComparison]::OrdinalIgnoreCase)
}

function Test-SecretPath {
    param([string]$Path)

    if ([string]::IsNullOrWhiteSpace($Path)) {
        return $false
    }

    $normalized = $Path.Replace("\", "/").ToLowerInvariant()
    $name = [System.IO.Path]::GetFileName($normalized)
    return $name -eq ".env" -or
        $name.StartsWith(".env.") -or
        $name.EndsWith(".pem") -or
        $name.EndsWith(".key") -or
        $name -eq "id_rsa" -or
        $name -eq "id_ed25519" -or
        $normalized.Contains("/.git/")
}

$toolName = [string]$payload.tool_name
$toolInput = $payload.tool_input

if ($toolName -match "^(Edit|Write|MultiEdit)$") {
    $filePath = $toolInput.file_path
    if ([string]::IsNullOrWhiteSpace($filePath)) {
        $filePath = $toolInput.path
    }

    if (-not (Test-InProject $filePath)) {
        Write-Decision "deny" "프로젝트 루트 밖 파일 수정은 차단됩니다: $filePath"
    }

    if (Test-SecretPath $filePath) {
        Write-Decision "deny" "민감 파일 또는 Git 내부 파일 수정은 차단됩니다: $filePath"
    }

    $normalizedPath = ([string]$filePath).Replace("\", "/").ToLowerInvariant()
    if ($normalizedPath.Contains(".claude/settings") -or $normalizedPath.Contains(".claude/hooks/")) {
        Write-Decision "ask" "Claude hook 설정 또는 hook 스크립트 변경입니다. 사용자 승인이 필요합니다: $filePath"
    }

    Write-Decision "ask" "파일 변경 작업입니다. 변경 의도를 확인한 뒤 승인해주세요: $filePath"
}

if ($toolName -eq "Bash") {
    $command = [string]$toolInput.command
    $lower = $command.ToLowerInvariant()

    if ($lower -match "(cat|type|get-content|gc)\s+.*\.env(\s|$)" -or $lower -match "(cat|type|get-content|gc)\s+.*(id_rsa|id_ed25519|\.pem|\.key)") {
        Write-Decision "deny" "민감 파일 내용을 출력하는 명령은 차단됩니다."
    }

    if ($lower -match "git\s+reset\s+--hard" -or $lower -match "git\s+clean\s+-[a-z]*f[a-z]*d|git\s+clean\s+-[a-z]*d[a-z]*f") {
        Write-Decision "deny" "파괴적인 Git reset/clean 명령은 차단됩니다."
    }

    if ($lower -match "(rm\s+-rf\s+[/\\]|remove-item\s+.*\.git|rm\s+.*\.git|rmdir\s+.*\.git)") {
        Write-Decision "deny" "루트 또는 .git 삭제 위험이 있는 명령은 차단됩니다."
    }

    if ($lower -match "git\s+(commit|push|tag|merge|rebase)\b") {
        Write-Decision "ask" "Git 이력 또는 원격 저장소에 영향을 주는 명령입니다. 사용자 승인이 필요합니다."
    }

    if ($lower -match "\b(remove-item|rm|del|rmdir)\b") {
        Write-Decision "ask" "삭제 명령입니다. 대상과 영향 범위를 확인한 뒤 승인해주세요."
    }

    if ($lower -match "\b(curl|wget|invoke-webrequest|iwr|invoke-restmethod|irm)\b") {
        Write-Decision "ask" "외부 네트워크 요청 명령입니다. 목적과 대상 URL을 확인한 뒤 승인해주세요."
    }

    if ($lower -match "\.claude[\\/](settings|hooks)") {
        Write-Decision "ask" "Claude hook 설정 또는 스크립트에 접근하는 명령입니다. 사용자 승인이 필요합니다."
    }
}

exit 0
