param(
    [Parameter(Position = 0)]
    [string]$Service = "member",
    [string]$BaseUrl = "",
    [string]$OutputPath = "",
    [switch]$DryRun
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Net.Http

$ServicePorts = @{
    gateway      = 8080
    product      = 8081
    payment      = 8082
    member       = 8083
    order        = 8084
    settlement   = 8085
    cart         = 8086
    notification = 8087
    ai           = 8088
    auction      = 8090
}

function Resolve-ServiceBaseUrl {
    param(
        [string]$TargetService,
        [string]$ExplicitBaseUrl
    )

    if (-not [string]::IsNullOrWhiteSpace($ExplicitBaseUrl)) {
        return $ExplicitBaseUrl.TrimEnd("/")
    }

    $normalizedService = $TargetService.Trim().ToLowerInvariant()
    if (-not $ServicePorts.ContainsKey($normalizedService)) {
        $knownServices = ($ServicePorts.Keys | Sort-Object) -join ", "
        throw "Unknown service: $TargetService. Available services: $knownServices"
    }

    return "http://localhost:$($ServicePorts[$normalizedService])"
}

function Resolve-OutputPath {
    param(
        [string]$TargetService,
        [string]$ExplicitOutputPath
    )

    if (-not [string]::IsNullOrWhiteSpace($ExplicitOutputPath)) {
        return $ExplicitOutputPath
    }

    $normalizedService = $TargetService.Trim().ToLowerInvariant()
    return Join-Path "docs/api/openapi" "$normalizedService.json"
}

function Export-OpenApi {
    param(
        [string]$TargetService,
        [string]$ResolvedBaseUrl,
        [string]$ResolvedOutputPath
    )

    $apiDocsUrl = "$ResolvedBaseUrl/v3/api-docs"
    $outputDirectory = Split-Path -Parent $ResolvedOutputPath

    Write-Host "[openapi-export] service: $TargetService" -ForegroundColor Green
    Write-Host "[openapi-export] url: $apiDocsUrl" -ForegroundColor Green
    Write-Host "[openapi-export] output: $ResolvedOutputPath" -ForegroundColor Green

    if ($DryRun) {
        Write-Host "[openapi-export] dry-run: no file will be written" -ForegroundColor Yellow
        return
    }

    if (-not (Test-Path -LiteralPath $outputDirectory)) {
        New-Item -ItemType Directory -Path $outputDirectory | Out-Null
    }

    $httpClient = [System.Net.Http.HttpClient]::new()
    $httpClient.Timeout = [TimeSpan]::FromSeconds(10)

    try {
        $response = $httpClient.GetAsync($apiDocsUrl).GetAwaiter().GetResult()
        $bytes = $response.Content.ReadAsByteArrayAsync().GetAwaiter().GetResult()
        if (-not $response.IsSuccessStatusCode) {
            $statusCode = [int]$response.StatusCode
            $reason = $response.ReasonPhrase
            throw "HTTP $statusCode $reason"
        }
    } catch {
        throw "Failed to fetch OpenAPI document from $apiDocsUrl. Cause: $($_.Exception.Message)"
    } finally {
        $httpClient.Dispose()
    }

    $content = [System.Text.Encoding]::UTF8.GetString($bytes)
    try {
        $null = $content | ConvertFrom-Json
    } catch {
        throw "OpenAPI response is not valid JSON: $apiDocsUrl"
    }

    Set-Content -LiteralPath $ResolvedOutputPath -Value $content -Encoding UTF8
    Write-Host "[openapi-export] done" -ForegroundColor Green
}

$resolvedBaseUrl = Resolve-ServiceBaseUrl -TargetService $Service -ExplicitBaseUrl $BaseUrl
$resolvedOutputPath = Resolve-OutputPath -TargetService $Service -ExplicitOutputPath $OutputPath

Export-OpenApi -TargetService $Service -ResolvedBaseUrl $resolvedBaseUrl -ResolvedOutputPath $resolvedOutputPath
