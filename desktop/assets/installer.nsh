; ═══════════════════════════════════════════════════════════
; Codinable NSIS カスタムスクリプト
;
;  1. 先頭に「言語選択 + ようこそ」ページを自分で描く。
;     NSIS 標準の言語選択ダイアログ (MUI_LANGDLL_DISPLAY) は素の Win32
;     コンボボックスで見た目が貧弱なため使わない
;     (electron-builder.js 側で displayLanguageSelector を false にしている)。
;     nsDialogs で描き、installerSidebar.bmp を左に出す。
;
;  2. 選ばれた言語 ($LANGUAGE = Windows の LCID) を
;     $INSTDIR\default-lang.txt に書き出す。
;     アプリ側は初回起動時だけこれを読む (src/main/config.js の readInstallerLang)。
;     2 回目以降はアプリの設定画面で切り替えた内容が優先される。
;
;  対応言語は日本語と英語の 2 つ。増やすときは
;    - ここの DropList / LanguagePageLeave / customInstall
;    - builder/electron-builder.js の installerLanguages
;    - src/app-config.js の PRODUCT.languages
;    - src/renderer/i18n.js の文言
;  をそろえる。
; ═══════════════════════════════════════════════════════════

!include LogicLib.nsh
!include WinMessages.nsh
!include nsDialogs.nsh

; 高 DPI 対応。指定しないと Windows が 96dpi 想定の描画を拡大するため、
; 高解像度ディスプレイで文字がぼやける。
!macro customHeader
  ManifestDPIAware true
!macroend

!define LANG_PAGE_BITMAP "${__FILEDIR__}\installerSidebar.bmp"

!macro customWelcomePage
  Page custom LanguagePageCreate LanguagePageLeave
!macroend

; アンインストーラのコンパイルパスにはこのページが挿入されない。
; 変数と Function を定義だけして使わないと「参照されていない」という
; 無害だが紛らわしい警告が出るため、インストーラ側に限定する。
!ifndef BUILD_UNINSTALLER

Var LangPageBitmapCtl
Var LangPageBitmapHandle
Var LangPageHeadingFont
Var LangPageSubFont
Var LangPageBodyFont
Var LangPageDropList

Function LanguagePageCreate
  nsDialogs::Create 1018
  Pop $0
  ${If} $0 == error
    Abort
  ${EndIf}

  InitPluginsDir
  File "/oname=$PLUGINSDIR\lang-side.bmp" "${LANG_PAGE_BITMAP}"

  ; 単位に u を付けてダイアログ単位で配置する。省略すると固定ピクセルになり、
  ; Windows の表示倍率を変えたときに崩れる。
  ${NSD_CreateBitmap} 0 0 62u 130u ""
  Pop $LangPageBitmapCtl
  ${NSD_SetStretchedImage} $LangPageBitmapCtl "$PLUGINSDIR\lang-side.bmp" $LangPageBitmapHandle

  CreateFont $LangPageHeadingFont "Segoe UI" 16 600
  CreateFont $LangPageSubFont     "Segoe UI" 11 400
  CreateFont $LangPageBodyFont    "Segoe UI" 10 400

  ${NSD_CreateLabel} 74u 14u 176u 16u "Select a language"
  Pop $1
  SendMessage $1 ${WM_SETFONT} $LangPageHeadingFont 1

  ${NSD_CreateLabel} 74u 32u 176u 12u "表示言語を選択してください"
  Pop $1
  SendMessage $1 ${WM_SETFONT} $LangPageSubFont 1

  ${NSD_CreateLabel} 74u 54u 170u 11u "Language / 言語"
  Pop $1
  SendMessage $1 ${WM_SETFONT} $LangPageBodyFont 1

  ${NSD_CreateDropList} 74u 68u 170u 100u ""
  Pop $LangPageDropList
  SendMessage $LangPageDropList ${WM_SETFONT} $LangPageBodyFont 1
  ${NSD_CB_AddString} $LangPageDropList "日本語"
  ${NSD_CB_AddString} $LangPageDropList "English"

  ; 既定は OS の表示言語に合わせる。$LANGUAGE には electron-builder が
  ; installerLanguages から解決した LCID が入っている。
  ${If} $LANGUAGE == 1041
    ${NSD_CB_SelectString} $LangPageDropList "日本語"
  ${Else}
    ${NSD_CB_SelectString} $LangPageDropList "English"
  ${EndIf}

  ${NSD_CreateLabel} 74u 92u 176u 32u "後からアプリの設定画面でも変更できます。$\r$\nYou can change this later in the app settings."
  Pop $1
  SendMessage $1 ${WM_SETFONT} $LangPageBodyFont 1

  nsDialogs::Show
FunctionEnd

Function LanguagePageLeave
  ${NSD_GetText} $LangPageDropList $0
  ${If} $0 == "日本語"
    StrCpy $LANGUAGE 1041
  ${Else}
    StrCpy $LANGUAGE 1033
  ${EndIf}
FunctionEnd

!endif ; BUILD_UNINSTALLER

; ── 選ばれた言語をインストール先に書き出す ───────────────────
!macro customInstall
  ${If} $LANGUAGE == 1041
    StrCpy $1 "ja"
  ${Else}
    StrCpy $1 "en"
  ${EndIf}

  FileOpen $0 "$INSTDIR\default-lang.txt" w
  FileWrite $0 $1
  FileClose $0
!macroend

!macro customUnInstall
  Delete "$INSTDIR\default-lang.txt"
!macroend

; ── アプリ実行中チェックの差し替え ───────────────────────────
;  electron-builder 既定の判定は「$INSTDIR 配下から起動しているプロセスが
;  1 つでもあればアプリが動いている」とみなす。
;
;  Codinable は受講者のコードを同梱 JDK
;  ($INSTDIR\resources\runtime\java\bin\java.exe) の子プロセスとして走らせるため、
;  アプリを閉じた後も java.exe (Gradle デーモン等) がしばらく居残る。これが既定の
;  判定に引っかかり、起動していないのに「終了できません」ダイアログが出てしまう。
;  居残りは $INSTDIR 配下のファイルを掴んだままなので、ファイル展開の失敗も招く。
;
;  そこで順序を変え、
;    1. $INSTDIR 配下の居残りプロセスは黙って強制終了する
;    2. そのうえでアプリ本体だけを対象に、確認 → 終了 → 再確認する
;  とする。ダイアログが出るのはアプリ本体が本当に動いているときだけになる。
;
;  このマクロを定義すると electron-builder 側の _CHECK_APP_RUNNING /
;  IS_POWERSHELL_AVAILABLE は挿入されない ($IsPowerShellAvailable や $pid も
;  定義されない) ため、ここではそれらを参照しない。
;  $CmdPath / $PowerShellPath は CHECK_APP_RUNNING が先に設定している。
!macro customCheckAppRunning
  ; 1) 居残りプロセス (同梱 JDK など) を静かに落とす。アプリ本体は 2) で扱う。
  ;    PowerShell が使えない環境では何も起きず、2) の本体チェックだけになる。
  nsExec::Exec `"$PowerShellPath" -NoProfile -NonInteractive -C "Get-CimInstance -ClassName Win32_Process | ? { $$_.Path -and $$_.Path.StartsWith('$INSTDIR\', 'CurrentCultureIgnoreCase') -and $$_.Name -ne '${APP_EXECUTABLE_FILENAME}' } | % { Stop-Process -Id $$_.ProcessId -Force -ErrorAction SilentlyContinue }"`
  Pop $0

  ; 2) アプリ本体だけを名前で判定する。findstr /B で CSV 行の先頭に一致させ、
  ;    部分一致の誤検知を避ける。
  StrCpy $R1 0
  codinableAppLoop:
    IntOp $R1 $R1 + 1
    nsExec::Exec `"$CmdPath" /C tasklist /FI "IMAGENAME eq ${APP_EXECUTABLE_FILENAME}" /NH /FO CSV | "$SYSDIR\findstr.exe" /B /I /C:"$\"${APP_EXECUTABLE_FILENAME}$\""`
    Pop $R0
    ${If} $R0 != 0
      Goto codinableAppNotRunning        ; 見つからない = 動いていない
    ${EndIf}

    ${If} $R1 == 1
      ; 初回だけ確認する (キャンセルならインストールを中止)
      MessageBox MB_OKCANCEL|MB_ICONEXCLAMATION "$(appRunning)" /SD IDOK IDOK codinableAppStop
      Quit
    ${ElseIf} $R1 > 3
      ; 3 回試しても終了できない (権限違い等)。手動で閉じてもらう
      MessageBox MB_RETRYCANCEL|MB_ICONEXCLAMATION "$(appCannotBeClosed)" /SD IDCANCEL IDRETRY codinableAppStop
      Quit
    ${EndIf}

  codinableAppStop:
    DetailPrint "$(appClosing)"
    nsExec::Exec `"$CmdPath" /C taskkill /F /IM "${APP_EXECUTABLE_FILENAME}" /FI "USERNAME eq %USERNAME%"`
    Pop $0
    Sleep 1000
    Goto codinableAppLoop

  codinableAppNotRunning:
!macroend
