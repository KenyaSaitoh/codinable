# Download portable messaging servers. No Windows service or global PATH changes.
param([string[]] $Only = @(), [switch] $Force)
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$repoRoot = Split-Path $PSScriptRoot -Parent
$runtimeRoot = [IO.Path]::GetFullPath((Join-Path $repoRoot 'runtime'))
$downloadRoot = Join-Path $env:TEMP 'codinable-messaging-downloads'
$stageRoot = Join-Path $runtimeRoot ('.messaging-setup-' + [Guid]::NewGuid().ToString('N'))
$tarExe = Join-Path $env:SystemRoot 'System32\tar.exe'
$Only = @($Only | ForEach-Object { $_ -split ',' })
# Read the literal version map without needing Node during the first setup.
$versionSource = Get-Content (Join-Path $repoRoot 'desktop\src\messaging-config.js') -Raw
function Version([string] $name) {
  $match = [regex]::Match($versionSource, ($name + ": '([0-9.]+)'"))
  if (-not $match.Success) { throw "Missing version: $name" }
  return $match.Groups[1].Value
}
function SafeRuntimePath([string] $target) {
  $full = [IO.Path]::GetFullPath($target)
  if (-not $full.StartsWith($runtimeRoot + '\', [StringComparison]::OrdinalIgnoreCase)) {
    throw "Path outside runtime directory: $full"
  }
  return $full
}
function Download([string] $url, [string] $target) {
  if (Test-Path -LiteralPath $target) { return }
  & (Join-Path $env:SystemRoot 'System32\curl.exe') -fL --retry 2 --connect-timeout 20 --max-time 300 --silent --show-error -o ($target + '.part') $url
  if ($LASTEXITCODE -ne 0) { throw "Download failed: $url" }
  Move-Item -LiteralPath ($target + '.part') -Destination $target -Force
}
New-Item -ItemType Directory -Force $runtimeRoot, $downloadRoot, $stageRoot | Out-Null
try {
  foreach ($name in @('kafka', 'erlang', 'rabbitmq')) {
    if ($Only.Count -and $Only -notcontains $name -and -not ($name -eq 'erlang' -and $Only -contains 'rabbitmq')) { continue }
    $version = Version $name
    $target = SafeRuntimePath (Join-Path $runtimeRoot $name)
    $marker = Join-Path $target 'codinable-version.txt'
    $portableReady = $name -ne 'erlang' -or ((Test-Path (Join-Path $target 'bin\vcruntime140.dll')) -and (Test-Path (Join-Path $target 'LICENSE.txt')))
    if (-not $Force -and $portableReady -and (Test-Path $marker) -and (Get-Content $marker -Raw).Trim() -eq $version) {
      Write-Host "[skip] $name $version"
      continue
    }
    Write-Host "[$name] Preparing $version..."
    $extract = SafeRuntimePath (Join-Path $stageRoot $name)
    New-Item -ItemType Directory -Path $extract -Force | Out-Null
    if ($name -eq 'kafka') {
      $filename = "kafka_2.13-$version.tgz"
      $url = "https://archive.apache.org/dist/kafka/$version/$filename"
      $archive = Join-Path $downloadRoot $filename
      Download $url $archive
      $checksum = (Invoke-WebRequest -UseBasicParsing -Uri ($url + '.sha512')).Content
      $expected = ([regex]::Match(($checksum -replace '\s', ''), '[a-fA-F0-9]{128}')).Value
      if (-not $expected -or (Get-FileHash $archive -Algorithm SHA512).Hash -ne $expected) { throw 'Kafka checksum mismatch' }
    } else {
      $repository = if ($name -eq 'erlang') { 'erlang/otp' } else { 'rabbitmq/rabbitmq-server' }
      $tag = if ($name -eq 'erlang') { "OTP-$version" } else { "v$version" }
      $filename = if ($name -eq 'erlang') { "otp_win64_$version.zip" } else { "rabbitmq-server-windows-$version.zip" }
      $archive = Join-Path $downloadRoot $filename
      $release = Invoke-RestMethod "https://api.github.com/repos/$repository/releases/tags/$tag"
      $asset = $release.assets | Where-Object { $_.name -eq $filename } | Select-Object -First 1
      if (-not $asset) { throw "Release asset missing: $filename" }
      Download $asset.browser_download_url $archive
      if ($asset.digest -like 'sha256:*') {
        if ((Get-FileHash $archive -Algorithm SHA256).Hash -ne $asset.digest.Substring(7)) { throw "Checksum mismatch: $filename" }
      }
    }
    & $tarExe -xf $archive -C $extract
    if ($LASTEXITCODE -ne 0) { throw "Extraction failed: $filename" }
    $source = if ($name -eq 'kafka') { Join-Path $extract "kafka_2.13-$version" }
              elseif ($name -eq 'rabbitmq') { Join-Path $extract "rabbitmq_server-$version" }
              else { $extract }
    if ($name -eq 'rabbitmq' -and -not (Test-Path $source)) { $source = Join-Path $extract "rabbitmq_$version" }
    if (-not (Test-Path $source)) { throw "Archive layout changed: $filename" }
    if ($name -eq 'erlang') {
      # The Erlang ZIP needs the VC runtime. Reuse the app-local redistributable
      # already shipped by Temurin so learners never need an elevated installer.
      $javaBin = Join-Path $runtimeRoot 'java\bin'
      $dlls = @('vcruntime140.dll', 'vcruntime140_1.dll', 'msvcp140.dll')
      $erts = Get-ChildItem -LiteralPath $source -Directory -Filter 'erts-*' | Select-Object -First 1
      if (-not $erts) { throw 'Erlang erts directory is missing' }
      foreach ($dll in $dlls) {
        $dllPath = Join-Path $javaBin $dll
        if (-not (Test-Path $dllPath)) { throw "Set up bundled Java first: $dllPath is missing" }
        Copy-Item -LiteralPath $dllPath -Destination (Join-Path $source "bin\$dll")
        Copy-Item -LiteralPath $dllPath -Destination (Join-Path $erts.FullName "bin\$dll")
      }
      Set-Content -LiteralPath (Join-Path $source 'CODINABLE-NOTICE.txt') -Encoding ASCII -Value 'The app-local Microsoft VC runtime DLLs are redistributed with the bundled Temurin runtime. See ../java/legal/java.base for notices. Erlang/OTP source: https://github.com/erlang/otp/tree/OTP-28.5'
      Download "https://raw.githubusercontent.com/erlang/otp/OTP-$version/LICENSE.txt" (Join-Path $source 'LICENSE.txt')
      $licenseDir = Join-Path $source 'LICENSES'
      New-Item -ItemType Directory -Path $licenseDir -Force | Out-Null
      $licenses = Invoke-RestMethod "https://api.github.com/repos/erlang/otp/contents/LICENSES?ref=OTP-$version"
      foreach ($license in $licenses) {
        if ($license.type -eq 'file') { Download $license.download_url (Join-Path $licenseDir $license.name) }
      }
    }
    if (Test-Path -LiteralPath $target) { Remove-Item -LiteralPath (SafeRuntimePath $target) -Recurse -Force }
    Move-Item -LiteralPath (SafeRuntimePath $source) -Destination $target
    Set-Content -LiteralPath $marker -Value $version -Encoding ASCII
    Write-Host "[$name] OK"
  }
} finally {
  if (Test-Path -LiteralPath $stageRoot) { Remove-Item -LiteralPath (SafeRuntimePath $stageRoot) -Recurse -Force }
}
