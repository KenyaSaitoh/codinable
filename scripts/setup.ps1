# ============================================================
#  Codinable - 開発環境セットアップスクリプト
#
#  git clone した直後のリポジトリに、同梱物 (git 管理外) を用意する
#  すでにあるものは飛ばすので、何度実行しても構わない
#
#    runtime/java/               jlink で最小化した JRE (javac 同梱, ~70MB)
#    runtime/node/               portable Node.js (node.exe のみ)
#    runtime/python/             embeddable Python
#    runtime/bash/               PortableGit から bash + coreutils + curl を抽出
#    resources/jdtls/            Eclipse JDT Language Server (Java の LSP)
#    resources/gradle-wrapper/   gradle-wrapper.jar (雛形に wrapper が無いとき用)
#    hsqldb/hsqldb-2.7.3.jar     SQL 学習用の組み込み DB
#
#  使い方:
#    powershell -ExecutionPolicy Bypass -File scripts\setup.ps1
#    powershell -ExecutionPolicy Bypass -File scripts\setup.ps1 -Only java,jdtls
# ============================================================
param(
  # 一部だけ入れ直したいとき: java / node / python / bash / jdtls / gradle / hsqldb
  [string[]] $Only = @(),
  # 既にあるものも作り直す
  [switch] $Force
)

$ErrorActionPreference = "Stop"
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$root      = Split-Path $PSScriptRoot -Parent
$runtime   = Join-Path $root "runtime"
$resources = Join-Path $root "resources"
$work      = Join-Path $env:TEMP "codinable-setup"

# ---- バージョン設定 ----
# Java は Spring Boot 4 / Java 25 の教材に合わせる。ここを上げるときは
# desktop/src/main/runtimes.js のコメントと講座側の要件も確認する
$jdkUrl     = "https://api.adoptium.net/v3/binary/latest/25/ga/windows/x64/jdk/hotspot/normal/eclipse"
$nodeVer    = "v24.18.0"
$nodeUrl    = "https://nodejs.org/dist/$nodeVer/node-$nodeVer-win-x64.zip"
$pyVer      = "3.13.14"
$pyUrl      = "https://www.python.org/ftp/python/$pyVer/python-$pyVer-embed-amd64.zip"
$jdtlsUrl   = "https://download.eclipse.org/jdtls/snapshots/jdt-language-server-latest.tar.gz"
$hsqldbVer  = "2.7.3"
$hsqldbUrl  = "https://repo1.maven.org/maven2/org/hsqldb/hsqldb/$hsqldbVer/hsqldb-$hsqldbVer.jar"
$gradleVer  = "9.6.1"
$gradleUrl  = "https://services.gradle.org/distributions/gradle-$gradleVer-bin.zip"

# jlink に含めるモジュール (javac + Spring Boot + HSQLDB + JUnit が動く構成)
$jlinkModules = "java.se,jdk.compiler,jdk.zipfs,jdk.charsets,jdk.crypto.ec," +
                "jdk.crypto.cryptoki,jdk.unsupported,jdk.management,jdk.httpserver,jdk.localedata"

# Windows 標準の bsdtar を **フルパスで** 呼ぶ。PATH 上に Git for Windows の
# GNU tar があると "C:\..." をリモートホスト指定と解釈して失敗する
$tarExe = Join-Path $env:SystemRoot "System32\tar.exe"

New-Item -ItemType Directory -Force $runtime, $resources, $work | Out-Null

function Want([string] $name) {
  return ($Only.Count -eq 0) -or ($Only -contains $name)
}

function Step([string] $name, [string] $marker, [ScriptBlock] $body) {
  if (-not (Want $name)) { return }
  if ((Test-Path $marker) -and -not $Force) {
    Write-Host "[skip] $name (既にあります)"
    return
  }
  Write-Host "[$name] 用意しています..."
  & $body
  Write-Host "[$name] OK"
}

# ---- Java (Temurin JDK をダウンロード → jlink で最小化) ----
Step "java" (Join-Path $runtime "java\bin\javac.exe") {
  $jdkZip = Join-Path $work "jdk.zip"
  Invoke-WebRequest -Uri $jdkUrl -OutFile $jdkZip -UseBasicParsing
  Expand-Archive -Path $jdkZip -DestinationPath (Join-Path $work "jdk") -Force
  $jdkHome = Get-ChildItem (Join-Path $work "jdk") -Directory | Select-Object -First 1

  $javaOut = Join-Path $runtime "java"
  if (Test-Path $javaOut) { Remove-Item -Recurse -Force $javaOut }
  & (Join-Path $jdkHome.FullName "bin\jlink.exe") `
      --add-modules $jlinkModules `
      --include-locales=en,ja `
      --strip-debug --no-header-files --no-man-pages --compress zip-6 `
      --output $javaOut
  if (-not (Test-Path (Join-Path $javaOut "bin\javac.exe"))) { throw "jlink に失敗しました" }
  Remove-Item -Recurse -Force (Join-Path $work "jdk") -ErrorAction SilentlyContinue
}

# ---- Node.js (node.exe + npm。TypeScript は type stripping で直接実行する) ----
# marker は npm.cmd にする。node.exe だと「npm を足す前の runtime/」を
# 完成済みと誤判定して skip してしまう (= npm スクリプトが動かない状態で止まる)
Step "node" (Join-Path $runtime "node\npm.cmd") {
  $nodeZip = Join-Path $work "node.zip"
  Invoke-WebRequest -Uri $nodeUrl -OutFile $nodeZip -UseBasicParsing
  Expand-Archive -Path $nodeZip -DestinationPath (Join-Path $work "node") -Force
  $nodeDir = Get-ChildItem (Join-Path $work "node") -Directory | Select-Object -First 1
  New-Item -ItemType Directory -Force (Join-Path $runtime "node") | Out-Null
  Copy-Item (Join-Path $nodeDir.FullName "node.exe") (Join-Path $runtime "node\node.exe") -Force
  Copy-Item (Join-Path $nodeDir.FullName "LICENSE")  (Join-Path $runtime "node\LICENSE")  -Force
  # npm / npx は node_modules ごと必要になる (React の教材で npm install を使う)
  Copy-Item -Recurse (Join-Path $nodeDir.FullName "node_modules") (Join-Path $runtime "node\node_modules") -Force
  foreach ($f in "npm","npm.cmd","npx","npx.cmd") {
    Copy-Item (Join-Path $nodeDir.FullName $f) (Join-Path $runtime "node\$f") -Force
  }
  Remove-Item -Recurse -Force (Join-Path $work "node") -ErrorAction SilentlyContinue
}

# ---- Python (embeddable + pip) ----
# embeddable 版は pip を持たず、._pth で sys.path を固定して site-packages も
# 読まない。そのままでは Django などを入れられないため、
#   1. ._pth に Lib\site-packages を足し、import site を有効にする
#   2. get-pip.py で pip を入れる
# の 2 手を踏んで「pip install できる Python」にする
Step "python" (Join-Path $runtime "python\Scripts\pip.exe") {
  $pyDir = Join-Path $runtime "python"
  $pyZip = Join-Path $work "python.zip"
  Invoke-WebRequest -Uri $pyUrl -OutFile $pyZip -UseBasicParsing
  Expand-Archive -Path $pyZip -DestinationPath $pyDir -Force

  $pth = Get-ChildItem $pyDir -Filter "python*._pth" | Select-Object -First 1
  if (-not $pth) { throw "python._pth not found" }
  # import site が無効なままだと site-packages も pip 本体も読み込まれない
  @(
    (Split-Path $pth.Name -Leaf) -replace '\._pth$', '.zip'
    "."
    "Lib\site-packages"
    "import site"
  ) | Set-Content -Path $pth.FullName -Encoding ASCII

  New-Item -ItemType Directory -Force (Join-Path $pyDir "Lib\site-packages") | Out-Null
  $getPip = Join-Path $work "get-pip.py"
  Invoke-WebRequest -Uri "https://bootstrap.pypa.io/get-pip.py" -OutFile $getPip -UseBasicParsing
  & (Join-Path $pyDir "python.exe") $getPip --no-warn-script-location
  if (-not (Test-Path (Join-Path $pyDir "Scripts\pip.exe"))) { throw "pip bootstrap failed" }
}

# ---- Bash (PortableGit から bash + coreutils + curl だけを抽出) ----
# フルの Git (~350MB) は同梱せず、シェル演習に要るものだけ残す
# PortableGit は 7-Zip 自己展開形式なので -y -o で無人展開できる
Step "bash" (Join-Path $runtime "bash\usr\bin\bash.exe") {
  $rel   = Invoke-RestMethod "https://api.github.com/repos/git-for-windows/git/releases/latest"
  $asset = $rel.assets | Where-Object { $_.name -like "PortableGit-*-64-bit.7z.exe" } | Select-Object -First 1
  if (-not $asset) { throw "PortableGit asset not found in latest git-for-windows release" }
  $gitSfx = Join-Path $work "PortableGit.7z.exe"
  Invoke-WebRequest -Uri $asset.browser_download_url -OutFile $gitSfx -UseBasicParsing
  $gitDir = Join-Path $work "portablegit"
  & $gitSfx -y ("-o" + $gitDir) | Out-Null
  if (-not (Test-Path (Join-Path $gitDir "usr\bin\bash.exe"))) { throw "PortableGit extraction failed" }

  $bashOut = Join-Path $runtime "bash"
  if (Test-Path $bashOut) { Remove-Item -Recurse -Force $bashOut }
  New-Item -ItemType Directory -Force (Join-Path $bashOut "usr\share") | Out-Null
  Copy-Item -Recurse (Join-Path $gitDir "usr\bin")            (Join-Path $bashOut "usr\bin")
  Copy-Item -Recurse (Join-Path $gitDir "usr\share\terminfo") (Join-Path $bashOut "usr\share\terminfo")
  # curl は API を叩く演習で使う。mingw64/bin の DLL 群と CA 証明書を添える
  New-Item -ItemType Directory -Force (Join-Path $bashOut "mingw64\bin") | Out-Null
  Copy-Item (Join-Path $gitDir "mingw64\bin\curl.exe") (Join-Path $bashOut "mingw64\bin\curl.exe")
  Copy-Item (Join-Path $gitDir "mingw64\bin\*.dll")    (Join-Path $bashOut "mingw64\bin\")
  $caBundle = Get-ChildItem (Join-Path $gitDir "mingw64") -Recurse -Filter "ca-bundle.crt" | Select-Object -First 1
  if ($caBundle) {
    New-Item -ItemType Directory -Force (Join-Path $bashOut "mingw64\etc\ssl\certs") | Out-Null
    Copy-Item $caBundle.FullName (Join-Path $bashOut "mingw64\etc\ssl\certs\ca-bundle.crt")
  }
  # bash 起動時の warning 抑止用 (msys の /tmp)
  New-Item -ItemType Directory -Force (Join-Path $bashOut "tmp") | Out-Null
  Remove-Item -Recurse -Force $gitDir -ErrorAction SilentlyContinue
}

# ---- jdtls (Java の言語サーバー。補完・定義ジャンプ・診断に使う) ----
# 実行は同梱 JRE (runtime/java) で行うので、別途 Java を入れる必要はない
Step "jdtls" (Join-Path $resources "jdtls\config_win") {
  if (-not (Test-Path $tarExe)) { throw "tar.exe が見つかりません: $tarExe" }
  $target = Join-Path $resources "jdtls"
  if (Test-Path $target) { Remove-Item -Recurse -Force $target }
  New-Item -ItemType Directory -Force $target | Out-Null

  $archive = Join-Path $work "jdtls.tar.gz"
  Invoke-WebRequest -Uri $jdtlsUrl -OutFile $archive -UseBasicParsing
  & $tarExe -xzf $archive -C $target
  if ($LASTEXITCODE -ne 0) { throw "jdtls の展開に失敗しました" }

  # 配布物には Linux / macOS 用の設定も入っているが Windows 版には要らない (~30MB 減)
  foreach ($d in "config_linux","config_linux_arm","config_mac","config_mac_arm",
                 "config_ss_linux","config_ss_linux_arm","config_ss_mac","config_ss_mac_arm",
                 "config_ss_win","bin") {
    Remove-Item -Recurse -Force (Join-Path $target $d) -ErrorAction SilentlyContinue
  }
  $launcher = Get-ChildItem (Join-Path $target "plugins") -Filter "org.eclipse.equinox.launcher_*.jar" `
              -ErrorAction SilentlyContinue | Select-Object -First 1
  if (-not $launcher) { throw "launcher jar が見つかりません (展開に失敗している可能性)" }
  if (-not (Test-Path (Join-Path $target "config_win"))) { throw "config_win が見つかりません" }
}

# ---- gradle-wrapper.jar ----
# Gradle 本体は同梱しない (雛形の gradlew が必要な版を自分で取ってくる)
# ただし wrapper の jar が欠けている雛形のために 1 つだけ手元に置く
# 参照側は desktop/src/main/runner.js の ensureGradleWrapper()
Step "gradle" (Join-Path $resources "gradle-wrapper\gradle-wrapper.jar") {
  $zip = Join-Path $work "gradle.zip"
  Invoke-WebRequest -Uri $gradleUrl -OutFile $zip -UseBasicParsing
  $ext = Join-Path $work "gradle"
  if (Test-Path $ext) { Remove-Item -Recurse -Force $ext }
  Expand-Archive -Path $zip -DestinationPath $ext -Force
  $jar = Get-ChildItem $ext -Recurse -Filter "gradle-wrapper.jar" | Select-Object -First 1
  if (-not $jar) { throw "gradle-wrapper.jar が見つかりません" }
  New-Item -ItemType Directory -Force (Join-Path $resources "gradle-wrapper") | Out-Null
  Copy-Item $jar.FullName (Join-Path $resources "gradle-wrapper\gradle-wrapper.jar") -Force
  Remove-Item -Recurse -Force $ext -ErrorAction SilentlyContinue
}

# ---- HSQLDB (SQL タブの組み込み DB) ----
Step "hsqldb" (Join-Path $root "hsqldb\hsqldb-$hsqldbVer.jar") {
  New-Item -ItemType Directory -Force (Join-Path $root "hsqldb") | Out-Null
  Invoke-WebRequest -Uri $hsqldbUrl -OutFile (Join-Path $root "hsqldb\hsqldb-$hsqldbVer.jar") -UseBasicParsing
}

Remove-Item -Recurse -Force $work -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "==== 動作確認 ===="
if (Test-Path (Join-Path $runtime "java\bin\java.exe"))   { & (Join-Path $runtime "java\bin\java.exe") -version }
if (Test-Path (Join-Path $runtime "node\node.exe"))       { & (Join-Path $runtime "node\node.exe") --version }
if (Test-Path (Join-Path $runtime "python\python.exe"))   { & (Join-Path $runtime "python\python.exe") --version }
if (Test-Path (Join-Path $runtime "bash\usr\bin\bash.exe")) {
  & (Join-Path $runtime "bash\usr\bin\bash.exe") --version | Select-Object -First 1
}
Write-Host ""
Write-Host "次の手順: cd desktop && npm install && npm start"
