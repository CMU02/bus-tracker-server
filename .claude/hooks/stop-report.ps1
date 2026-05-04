$ErrorActionPreference = "Continue"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$status = git status --short 2>$null
if (-not [string]::IsNullOrWhiteSpace($status)) {
    Write-Output "Guardrail summary: working tree has changes. Report the completed task and mention verification status."
}

$progressPath = ".agents/rules/project-progress.md"
if (Test-Path -LiteralPath $progressPath) {
    $progress = Get-Content -LiteralPath $progressPath -Raw
    $implementationDone = $progress -match "- \[x\] 구현 완료"
    $testsDone = $progress -match "- \[x\] 테스트 완료"
    $prdDone = $progress -match "- \[x\] 다음 기능 PRD Notion 작성 완료"

    if ($implementationDone -and $testsDone -and -not $prdDone) {
        [Console]::Error.WriteLine("Guardrail block: 구현 완료와 테스트 완료가 체크됐지만 다음 기능 PRD가 Notion에 작성되지 않았습니다. BusTracker Notion 페이지에 다음 기능 PRD를 작성한 뒤 project-progress.md의 PRD 항목을 체크해주세요.")
        exit 2
    }
}

exit 0
