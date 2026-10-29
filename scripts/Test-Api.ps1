param(
    [string]$BaseUri = 'http://127.0.0.1:8080'
)

$ErrorActionPreference = 'Stop'
$api = $BaseUri.TrimEnd('/') + '/api/v1/students'
$workDirectory = Join-Path ([System.IO.Path]::GetTempPath()) ('student-ojt-api-' + [guid]::NewGuid().ToString('N'))
$requestFile = Join-Path $workDirectory 'request.json'
$responseFile = Join-Path $workDirectory 'response.json'
$utf8 = [System.Text.UTF8Encoding]::new($false)
New-Item -ItemType Directory -Path $workDirectory | Out-Null

function Assert-Condition {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) { throw $Message }
}

function Invoke-Api {
    param([string]$Method, [string]$Path, [object]$Payload, [string]$RawJson)
    $curlArguments = @('--silent', '--show-error', '--connect-timeout', '5', '--max-time', '15',
        '-X', $Method, ($api + $Path), '-H', 'Content-Type: application/json',
        '--output', $responseFile, '--write-out', '%{http_code}')
    if ($null -ne $Payload -or $PSBoundParameters.ContainsKey('RawJson')) {
        $json = if ($PSBoundParameters.ContainsKey('RawJson')) { $RawJson } else { $Payload | ConvertTo-Json -Depth 5 }
        [System.IO.File]::WriteAllText($requestFile, $json, $utf8)
        $curlArguments += @('--data-binary', ('@' + $requestFile))
    }
    $statusText = & curl.exe @curlArguments
    if ($LASTEXITCODE -ne 0) { throw "curl failed with exit code $LASTEXITCODE" }
    $raw = [System.IO.File]::ReadAllText($responseFile, $utf8)
    return [pscustomobject]@{ Status = [int]$statusText; Body = ($raw | ConvertFrom-Json); Raw = $raw }
}

function Assert-Error {
    param([object]$Response, [int]$Status, [string]$Message, [bool]$WithErrors = $false)
    Assert-Condition ($Response.Status -eq $Status) "Expected HTTP $Status, received $($Response.Status)"
    Assert-Condition ($Response.Body.status -eq $Status) 'Error body status differs from HTTP status'
    Assert-Condition ($Response.Body.message -eq $Message) 'Error message differs from the API contract'
    $expected = @('timestamp', 'status', 'message')
    if ($WithErrors) { $expected += 'errors' }
    $actualNames = @($Response.Body.PSObject.Properties.Name | Sort-Object)
    Assert-Condition (($actualNames -join ',') -eq (($expected | Sort-Object) -join ',')) 'Unexpected error fields'
    [void][System.DateTimeOffset]::Parse($Response.Body.timestamp)
    if ($WithErrors) {
        Assert-Condition ($Response.Body.errors -is [pscustomobject]) 'errors must be an object'
    }
    Assert-Condition ($Response.Raw -notmatch 'stackTrace|org\.hibernate|org\.h2|SELECT |INSERT ') 'Internal details leaked'
}

try {
    $suffix = [guid]::NewGuid().ToString('N')
    $nationalId = '079' + (Get-Random -Minimum 100000000 -Maximum 999999999).ToString()
    $payload = @{
        fullName = 'Sinh vien kiem thu'
        email = "student-$suffix@example.test"
        phone = '0901234567'
        nationalId = $nationalId
        university = 'Truong dai hoc mau'
        major = 'CNPM'
    }

    $created = Invoke-Api -Method POST -Path '/register' -Payload $payload
    Assert-Condition ($created.Status -eq 201) 'Registration must return 201'
    [void][guid]::Parse($created.Body.id)
    $successFields = @('id', 'fullName', 'email', 'maskedPhone', 'maskedNationalId', 'university', 'major', 'status')
    Assert-Condition ((@($created.Body.PSObject.Properties.Name | Sort-Object) -join ',') -eq
        (($successFields | Sort-Object) -join ',')) 'Registration must return exactly eight fields'
    Assert-Condition ($created.Body.maskedPhone -eq '090*****67') 'Phone masking differs from contract'
    Assert-Condition ($created.Body.maskedNationalId -eq ('079******' + $nationalId.Substring(9))) 'National ID masking differs from contract'
    Assert-Condition ($created.Body.major -eq 'CNPM' -and $created.Body.status -eq 'PENDING_APPROVAL') 'Missing major or default status'
    Assert-Condition (-not $created.Raw.Contains($nationalId) -and -not $created.Raw.Contains('0901234567')) 'Raw PII leaked'
    Write-Host 'PASS 01 POST 201, UUID, eight fields and masking'

    $fetched = Invoke-Api -Method GET -Path ('/' + $created.Body.id)
    Assert-Condition ($fetched.Status -eq 200) 'Lookup must return 200'
    foreach ($field in $successFields) {
        Assert-Condition ($fetched.Body.$field -eq $created.Body.$field) "GET differs from POST at $field"
    }
    Write-Host 'PASS 02 GET 200 matches registration response'

    $invalid = $payload.Clone()
    $invalid.phone = '123'
    $invalid.nationalId = '456'
    $validation = Invoke-Api -Method POST -Path '/register' -Payload $invalid
    Assert-Error $validation 400 'Dữ liệu đầu vào không hợp lệ' $true
    Assert-Condition ($validation.Body.errors.phone -eq 'Số điện thoại phải đủ 10 chữ số hợp lệ tại Việt Nam') 'Wrong phone message'
    Assert-Condition ($validation.Body.errors.nationalId -eq 'Số CCCD phải đúng 12 chữ số') 'Wrong national ID message'
    Write-Host 'PASS 03 validation 400 with field errors object'

    $duplicateEmail = $payload.Clone()
    $duplicateEmail.nationalId = '078' + $nationalId.Substring(3)
    Assert-Error (Invoke-Api -Method POST -Path '/register' -Payload $duplicateEmail) 409 'Số CCCD hoặc Email đã tồn tại trên hệ thống OJT'
    Write-Host 'PASS 04 duplicate email 409'

    $duplicateNationalId = $payload.Clone()
    $duplicateNationalId.email = "other-$suffix@example.test"
    Assert-Error (Invoke-Api -Method POST -Path '/register' -Payload $duplicateNationalId) 409 'Số CCCD hoặc Email đã tồn tại trên hệ thống OJT'
    Write-Host 'PASS 05 duplicate national ID 409'

    Assert-Error (Invoke-Api -Method GET -Path ('/' + [guid]::NewGuid())) 404 'Không tìm thấy hồ sơ sinh viên với ID cung cấp'
    Write-Host 'PASS 06 missing UUID 404'

    Assert-Error (Invoke-Api -Method GET -Path '/not-a-uuid') 400 'Dữ liệu đầu vào không hợp lệ' $true
    Write-Host 'PASS 07 malformed UUID 400'

    Assert-Error (Invoke-Api -Method POST -Path '/register' -RawJson '{invalid') 400 'Dữ liệu đầu vào không hợp lệ' $true
    Write-Host 'PASS 08 malformed JSON 400'

    $wrongMethod = Invoke-Api -Method PUT -Path ('/' + $created.Body.id)
    Assert-Condition ($wrongMethod.Status -eq 405) 'Unsupported method must remain 405'
    Write-Host 'PASS 09 unsupported method remains 405'

    # Cố ý vượt độ dài cột DB để kiểm tra lỗi kỹ thuật không bị gán thành duplicate hoặc lộ SQL.
    $technical = $payload.Clone()
    $technical.fullName = 'x' * 300
    $technical.email = "technical-$suffix@example.test"
    $technical.nationalId = '077' + $nationalId.Substring(3)
    $failure = Invoke-Api -Method POST -Path '/register?trace=true&message=true' -Payload $technical
    Assert-Condition ($failure.Status -eq 500) 'Unrelated database error must not become duplicate 409'
    $forbidden = @('trace', 'exception', 'message', 'sql')
    foreach ($field in $forbidden) {
        Assert-Condition (-not ($failure.Body.PSObject.Properties.Name -contains $field)) "Technical error leaked $field"
    }
    Assert-Condition ($failure.Raw -notmatch 'org\.hibernate|org\.h2|INSERT |student_registrations') 'SQL details leaked'
    Assert-Condition (-not $failure.Raw.Contains($technical.nationalId)) 'National ID leaked through technical error'
    Write-Host 'PASS 10 technical DB error stays 500 without SQL/trace/PII'

    Write-Host 'RESULT 10/10 cURL checks passed'
} finally {
    foreach ($file in @($requestFile, $responseFile)) {
        if (Test-Path -LiteralPath $file) { Remove-Item -LiteralPath $file -Force }
    }
    # Chỉ xóa thư mục tạm rỗng do chính lần chạy này tạo; không xóa đệ quy.
    Remove-Item -LiteralPath $workDirectory -Force
}
