// ═══════════════════════════════════════════════════════════
//  UI 文言 (日本語 / 英語)
//
//  キーは index.html の data-i18n* 属性、または renderer.js の t() / tf() から引く
//    data-i18n             → textContent
//    data-i18n-title       → title 属性
//    data-i18n-placeholder → placeholder 属性
//
//  対応言語を増やすときは UI_STRINGS にキーを足し、
//  src/app-config.js の PRODUCT.languages にも追加する
// ═══════════════════════════════════════════════════════════

const UI_STRINGS = {
  ja: {
    tabMessaging: 'メッセージング',
    messagingHint: '必要なサーバーを起動して、演習から接続できます。データは停止後も保存されます。',
    messagingLog: 'サーバーログ',
    messagingStart: '起動',
    messagingStop: '停止',
    messagingReset: 'データを初期化',
    messagingManagement: '管理画面',
    messagingCredentials: 'ユーザー名 / パスワード: guest / guest',
    messagingConfirmReset: '{name} の保存データを削除します。メッセージやトピック・キューの設定は元に戻せません。初期化しますか？',
    messaging_stopped: '停止中',
    messaging_starting: '起動中…',
    messaging_running: '稼働中',
    messaging_stopping: '停止処理中…',
    messaging_error: 'エラー',
    // ── ヘッダー / プロジェクト ──
    noProject:           'プロジェクト未選択',
    btnResetExercise:    '↺ 初期化',
    btnResetExerciseTitle:
      'この演習のファイルを配布時の状態に戻します（自分で足したファイルは残ります）',
    llmPickerTitle:      'チャットに使うモデル（API キーは自分で設定します）',
    btnSettings:         '⚙ 設定',
    settings:            '設定',
    close:               '閉じる',

    // ── 設定 ──
    settingsTitle:       '⚙ 設定',
    uiLangLabel:         '表示言語',
    theme:               'テーマ',
    fontSize:            'フォントサイズ',
    fontFamily:          'フォント',
    keybinding:          'キーバインド',
    keybindingDefault:   '標準',
    llmSectionLabel:     'LLM チャット (任意)',
    llmSectionHint:      'チャットは補助機能です。キーを登録しなくても、開発環境としてはすべて動きます。',
    llmOverrideLabel:    'モデル ID の上書き (空欄なら既定値)',
    llmDefaultModel:     '既定: {model}',
    apiKeySet:           '設定済み',
    apiKeyUnset:         '未設定',
    apiKeyGet:           'キーを取得',
    wsLabel:             'ワークスペース (作業ファイルの保存先)',
    wsHint:              'このフォルダ直下のディレクトリが 1 つのプロジェクトになります',
    btnChange:           '変更',
    runtimeLabel:        '同梱ランタイム',
    runtimeMissing:      '未セットアップ',
    versionLabel:        'バージョン',
    loading:             '読み込み中...',
    btnSave:             '保存',
    btnCancel:           'キャンセル',
    settingsSaved:       '設定を保存しました。',
    wsChangedReload:     'ワークスペースを変更しました。プロジェクト一覧を読み込み直します。',

    // ── 演習一覧 ──
    // 演習 = 講座のレッスンに対応する「動かして確かめる 1 単位」
    // 問題を解かせるものではないので、正解・採点・完了といった語は使わない
    exerciseTitle:       '📚 演習',
    exerciseEmpty:       '演習がありません',

    // ── コース (講座) ──
    // 講座は 1 つずつ足せる。同梱 / 共有 / 個人のどこから読まれたかを設定画面に出す
    coursePickerTitle:   '受講している講座を選びます',
    coursesLabel:        'インストールされている講座',
    coursesHint:         '講座を足すと、このアプリを入れ直さなくても演習が増えます。',
    coursesEmpty:        '講座が入っていません',
    coursesExercises:    '演習 {n} 件',
    coursesRootMissing:  '（まだありません）',
    coursesDevFilter:    '開発用の絞り込み（npm start）',
    courseSource_bundled: 'アプリ同梱',
    courseSource_shared:  '共有フォルダ',
    courseSource_user:    '個人フォルダ',
    btnOpenCoursesDir:      '📂 講座フォルダを開く',
    btnOpenCoursesDirTitle: '講座を手で足すためのフォルダを開きます',
    btnReloadCourses:       '↻ 読み直す',
    btnReloadCoursesTitle:  '足した講座を読み直します',
    exerciseChapter:     'チャプター {n}',
    exerciseNotCreated:  'まだ開いていません（選ぶと雛形から用意します）',
    runtime_java:        'Java',
    runtime_spring:      'Spring',
    runtime_node:        'Node.js',
    runtime_react:       'React',
    runtime_python:      'Python',
    runtime_static:      '静的Web',
    runtime_sql:         'SQL',
    runtime_shell:       'Shell',
    runtime_other:       'その他',

    // ── ファイルツリー ──
    explorerTitle:       '📁 プロジェクト',
    explorerEmpty:       'プロジェクトを選択してください',
    btnNewFile:          '新しいファイル',
    btnNewDir:           '新しいフォルダ',
    btnRefreshTree:      '再読み込み',
    dragToResizeWidth:   'ドラッグで幅を調整',
    dragToResizeHeight:  'ドラッグで高さを調整',
    promptNewFile:       '新しいファイルのパスを入力してください（例: src/main/java/App.java）',
    promptNewDir:        '新しいフォルダのパスを入力してください（例: src/main/resources）',
    promptRename:        '新しいパスを入力してください:',
    ctxOpen:             '開く',
    ctxRename:           '✎ 名前の変更',
    ctxDelete:           '✕ 削除',
    ctxReveal:           '📂 エクスプローラで表示',
    ctxPreview:          '🌐 プレビュー',
    ctxRun:              '▶ このファイルを実行',
    confirmDeleteEntry:  '「{name}」を削除しますか？ この操作は取り消せません。',

    // ── エディタ ──
    editorTitle:         '💻 エディタ',
    editorEmpty:         '左のファイルツリーからファイルを開いてください',
    btnUndo:             '↺ 元に戻す',
    btnUndoTitle:        '直前の編集を元に戻します（Ctrl/Cmd+Z）',
    btnRedo:             '↻ やり直す',
    btnRedoTitle:        '元に戻した編集をやり直します（Ctrl/Cmd+Y）',
    btnMdPreview:        '👁 プレビュー',
    btnMdPreviewTitle:   'Markdown のプレビューと編集を切り替えます',
    btnMdEdit:           '✎ 編集',
    saved:               '保存しました',
    saveFailed:          '保存できませんでした: {error}',
    openFailedBinary:    'このファイルはテキストとして開けません。',
    openFailedTooLarge:  'ファイルが大きすぎるため開けません（2MB まで）。',
    openFailed:          'ファイルを開けませんでした: {error}',
    confirmCloseDirty:   '「{name}」は保存されていません。閉じますか？',

    // ── 実行 ──
    runTargetTitle:      '実行する内容を選びます',
    runTargetFile:       '▶ {name}',
    runTargetSql:        '🗄 SQL: {name}',
    runNotRunnable:      '{name} は実行対象になっていません。',
    runTargetGradle:     'Gradle: {task}',
    runTargetNpm:        'npm: {script}',
    runTargetJava:       '▶ Java (main を探して実行)',
    runTargetStatic:     '🌐 静的ページをプレビュー',
    runTargetNone:       '実行できるものがありません',
    btnRun:              '実行',
    btnStop:             '停止',
    btnPreview:          '🌐 プレビュー',
    btnPreviewTitle:     'Web プレビュータブで開きます',
    hsqldbAvailable:     'HSQLDB が利用可能です',
    runNoProject:        '先にプロジェクトを選択してください。',
    runNoTarget:         '実行する内容を選んでください。',
    runFailed:           '実行を開始できませんでした: {error}',
    runExitOk:           '\n✅ 正常終了しました (exit code 0)\n',
    runExitNg:           '\n❌ 異常終了しました (exit code {code})\n',
    runStopped:          '\n⏹ 停止しました\n',

    // ── 出力タブ ──
    tabOutput:           '実行結果',
    tabTests:            'テスト結果',
    tabTestsTitle:       'テストメソッド単位の成否・失敗内容・カバレッジ',
    tabSql:              '🗄 SQL',
    tabSqlTitle:         'HSQLDB に SQL を実行します',
    tabTerminal:         '🖥 ターミナル',
    tabTerminalTitle:    'プロジェクトのフォルダで動くターミナル (bash)',
    tabBrowser:          '🌐 プレビュー',
    tabBrowserTitle:     'Web アプリ・静的ページの動作確認',
    outputPlaceholder:   'ここに実行結果が表示されます。',
    btnAddContext:       '＋ チャットに渡す',
    btnAddContextTitle:  'この実行結果をチャットの参考情報に追加します',
    runStdinPlaceholder: '標準入力へ送信（Enter）',
    runStdinSend:        '送信',

    // ── テスト結果 ──
    testResultsPlaceholder:
      'Gradle の test タスクを実行すると、テストメソッド単位の結果がここに出ます。',
    testsRunning:        'テストを実行しています...',
    testsNoResults:      'テスト結果が見つかりませんでした（コンパイルエラー、または対象テストが 0 件です）。',
    testsAllPassed:      'すべて成功',
    testsFailedCount:    '{n} 件失敗',
    testsCoverage:       'カバレッジ',
    testsExpected:       '期待値',
    testsActual:         '実測値',
    testsStackTrace:     'スタックトレース',
    testsCoverageByFile: 'ファイル別カバレッジ',
    testsCovLegend:      '緑 = 実行された行 / 黄 = 分岐の一部のみ / 赤 = 未実行',
    testsCoverageOpenHint: 'クリックするとこのファイルを開き、行ごとのカバレッジを表示します',

    // ── SQL ──
    btnSqlStart:         'DB起動',
    btnSqlStop:          'DB停止',
    btnSqlRun:           'SQL実行',
    sqlHint:             'エディタで開いている .sql の内容（選択中ならその部分）を実行します',
    sqlPlaceholder:      '「DB起動」してから「SQL実行」を押してください。',
    sqlStarting:         'HSQLDB を起動しています...',
    sqlStarted:          'HSQLDB を起動しました。SQL を実行できます。',
    sqlStopped:          'HSQLDB を停止しました。',
    sqlNoSql:            '実行する SQL がありません。エディタで .sql ファイルを開いてください。',
    sqlRowCount:         '{n} 件',
    sqlUpdated:          '{n} 件更新しました。',
    sqlOk:               '実行しました。',
    sqlNull:             '(NULL)',

    // ── ターミナル ──
    termNoProject:       'プロジェクトを選択するとターミナルが使えます。\r\n',
    termExited:          '\r\n[プロセスが終了しました (exit code {code})]\r\n',

    // ── プレビュー ──
    browserBack:         '戻る',
    browserForward:      '進む',
    browserReload:       '再読み込み',
    browserGo:           '開く',
    browserExternal:     '外部ブラウザで開く',
    previewNoTarget:     'プレビューできる対象が見つかりません（index.html があるか、アプリが起動しているか確認してください）。',
    previewServeFailed:  '静的ページを配信できませんでした: {error}',

    // ── プロジェクト (演習から作る) ──
    errCreateFailed:     'プロジェクトを作成できませんでした: {error}',
    resetNoTemplate:     'このプロジェクトは演習の雛形から作られていないため、戻す先がありません。',
    confirmResetExercise:
      '{name} を配布時の状態に戻します。\n編集した内容は失われます（自分で足したファイルは残ります）。\nよろしいですか？',
    resetDone:           '初期化しました（{n} 件のファイルを書き戻し）。',

    // ── チャット ──
    chatTitle:           '🤖 チャット',
    chatWelcome1:        '🤖 コードの相談相手です。使うかどうかは自由です。',
    chatWelcome2:        '📄 開いているプロジェクトのファイルは、送信時に自動で渡します。',
    chatWelcome3:        '🔑 使う前に ⚙ 設定で API キーを登録してください。',
    chatInputAsk:        '質問を入力... (Enter で送信 / Shift+Enter で改行)',
    chatInputAgent:      'この演習で直してほしいことを入力... (Enter で送信 / Shift+Enter で改行)',
    btnChatClear:        '⌫ クリア',
    btnChatClearTitle:   '会話履歴を消します',

    // ── Ask / Agent の切り替え ──
    chatModeLabel:       'AIモード',
    modeAsk:             'Ask',
    modeAgent:           'Agent',
    chatModeTitle:       'Ask は読むだけ。Agent は開いている演習のファイルを書き換えます（実行はしません）',

    // ── コードの書き換え (AI駆動開発) ──
    // Ask で返ってきた変更案は「適用」を押してから反映する
    // Agent は自分で書き換えるので、代わりに「元に戻す」を出す
    agentNoProject:      '演習を選んでから Agent に依頼してください。',
    agentToolList:       'ファイル一覧を見る',
    agentToolRead:       'ファイルを読む',
    agentToolWrite:      'ファイルを書き換える',
    agentToolOther:      '作業する',
    agentLimit:          '手順が {n} 回に達したので、ここで止めました。続けるにはもう一度依頼してください。',
    editApplied:         '書き換えました',
    editUndo:            '元に戻す',
    editUndone:          '元に戻しました',
    editNewFile:         '新規ファイル',
    editDiffSkipped:     '… 変更のない {n} 行',
    btnSend:             '➤ 送信',
    btnSendTitle:        '送信',
    btnAbort:            '⏹ 中断',
    btnAbortTitle:       '応答を中断します',
    roleUser:            'あなた',
    roleAssistant:       '{model}',
    chatNoKey:           '{label} の API キーが未設定です。⚙ 設定から登録してください。',
    chatError:           'エラー: {error}',
    chipProject:         '📁 {name} のファイル {n} 件',
    chipLog:             '📋 実行ログ',
    chipRemove:          'この添付を外す',

    // ── 言語サーバー ──
    lspStarting:         'Java 言語サーバーを起動しています...',
    lspReady:            'Java 言語サーバー: 接続済み',
    lspError:            'Java 言語サーバーを起動できませんでした',

    ok:                  'OK',
  },

  en: {
    tabMessaging: 'Messaging',
    messagingHint: 'Start a server to connect from your exercises. Data is kept when the server stops.',
    messagingLog: 'Server log',
    messagingStart: 'Start',
    messagingStop: 'Stop',
    messagingReset: 'Reset data',
    messagingManagement: 'Management UI',
    messagingCredentials: 'Username / password: guest / guest',
    messagingConfirmReset: 'Delete all saved {name} data? Messages, topics, and queue settings cannot be restored.',
    messaging_stopped: 'Stopped',
    messaging_starting: 'Starting…',
    messaging_running: 'Running',
    messaging_stopping: 'Stopping…',
    messaging_error: 'Error',
    noProject:           'No project selected',
    btnResetExercise:    '↺ Reset',
    btnResetExerciseTitle:
      'Put this exercise back to the state it shipped in (files you added are kept)',
    llmPickerTitle:      'Model used for chat (you supply the API key)',
    btnSettings:         '⚙ Settings',
    settings:            'Settings',
    close:               'Close',

    settingsTitle:       '⚙ Settings',
    uiLangLabel:         'Display language',
    theme:               'Theme',
    fontSize:            'Font size',
    fontFamily:          'Font',
    keybinding:          'Key bindings',
    keybindingDefault:   'Default',
    llmSectionLabel:     'LLM chat (optional)',
    llmSectionHint:      'Chat is a side feature. Everything else works without an API key.',
    llmOverrideLabel:    'Override model ID (leave empty for the default)',
    llmDefaultModel:     'Default: {model}',
    apiKeySet:           'Configured',
    apiKeyUnset:         'Not set',
    apiKeyGet:           'Get a key',
    wsLabel:             'Workspace (where your files are kept)',
    wsHint:              'Each directory directly under this folder is one project',
    btnChange:           'Change',
    runtimeLabel:        'Bundled runtimes',
    runtimeMissing:      'not installed',
    versionLabel:        'Version',
    loading:             'Loading...',
    btnSave:             'Save',
    btnCancel:           'Cancel',
    settingsSaved:       'Settings saved.',
    wsChangedReload:     'Workspace changed. Reloading the project list.',

    exerciseTitle:       '📚 Exercises',
    exerciseEmpty:       'No exercises',

    // ── Courses ──
    coursePickerTitle:   'Choose the course you are taking',
    coursesLabel:        'Installed courses',
    coursesHint:         'Adding a course brings in more exercises without reinstalling the app.',
    coursesEmpty:        'No courses installed',
    coursesExercises:    '{n} exercises',
    coursesRootMissing:  '(not present yet)',
    coursesDevFilter:    'Development filter (npm start)',
    courseSource_bundled: 'Bundled with app',
    courseSource_shared:  'Shared folder',
    courseSource_user:    'Personal folder',
    btnOpenCoursesDir:      '📂 Open course folder',
    btnOpenCoursesDirTitle: 'Opens the folder where you can add courses by hand',
    btnReloadCourses:       '↻ Reload',
    btnReloadCoursesTitle:  'Reloads courses you have added',
    exerciseChapter:     'Chapter {n}',
    exerciseNotCreated:  'Not opened yet (choosing it sets up the files)',
    runtime_java:        'Java',
    runtime_spring:      'Spring',
    runtime_node:        'Node.js',
    runtime_react:       'React',
    runtime_python:      'Python',
    runtime_static:      'Static web',
    runtime_sql:         'SQL',
    runtime_shell:       'Shell',
    runtime_other:       'Other',

    explorerTitle:       '📁 Project',
    explorerEmpty:       'Select a project first',
    btnNewFile:          'New file',
    btnNewDir:           'New folder',
    btnRefreshTree:      'Reload',
    dragToResizeWidth:   'Drag to resize width',
    dragToResizeHeight:  'Drag to resize height',
    promptNewFile:       'Path of the new file (e.g. src/main/java/App.java)',
    promptNewDir:        'Path of the new folder (e.g. src/main/resources)',
    promptRename:        'Enter the new path:',
    ctxOpen:             'Open',
    ctxRename:           '✎ Rename',
    ctxDelete:           '✕ Delete',
    ctxReveal:           '📂 Show in file manager',
    ctxPreview:          '🌐 Preview',
    ctxRun:              '▶ Run this file',
    confirmDeleteEntry:  'Delete "{name}"? This cannot be undone.',

    editorTitle:         '💻 Editor',
    editorEmpty:         'Open a file from the tree on the left',
    btnUndo:             '↺ Undo',
    btnUndoTitle:        'Undo the last edit (Ctrl/Cmd+Z)',
    btnRedo:             '↻ Redo',
    btnRedoTitle:        'Redo the edit you undid (Ctrl/Cmd+Y)',
    btnMdPreview:        '👁 Preview',
    btnMdPreviewTitle:   'Switch between Markdown preview and editing',
    btnMdEdit:           '✎ Edit',
    saved:               'Saved',
    saveFailed:          'Could not save: {error}',
    openFailedBinary:    'This file cannot be opened as text.',
    openFailedTooLarge:  'This file is too large to open (2MB limit).',
    openFailed:          'Could not open the file: {error}',
    confirmCloseDirty:   '"{name}" has unsaved changes. Close it anyway?',

    runTargetTitle:      'Choose what to run',
    runTargetFile:       '▶ {name}',
    runTargetSql:        '🗄 SQL: {name}',
    runNotRunnable:      '{name} is not a run target.',
    runTargetGradle:     'Gradle: {task}',
    runTargetNpm:        'npm: {script}',
    runTargetJava:       '▶ Java (find and run main)',
    runTargetStatic:     '🌐 Preview static page',
    runTargetNone:       'Nothing to run',
    btnRun:              'Run',
    btnStop:             'Stop',
    btnPreview:          '🌐 Preview',
    btnPreviewTitle:     'Open in the web preview tab',
    hsqldbAvailable:     'HSQLDB is available',
    runNoProject:        'Select a project first.',
    runNoTarget:         'Choose what to run.',
    runFailed:           'Could not start: {error}',
    runExitOk:           '\n✅ Finished successfully (exit code 0)\n',
    runExitNg:           '\n❌ Failed (exit code {code})\n',
    runStopped:          '\n⏹ Stopped\n',

    tabOutput:           'Output',
    tabTests:            'Tests',
    tabTestsTitle:       'Per-method results, failure details and coverage',
    tabSql:              '🗄 SQL',
    tabSqlTitle:         'Run SQL against HSQLDB',
    tabTerminal:         '🖥 Terminal',
    tabTerminalTitle:    'A terminal (bash) running in the project folder',
    tabBrowser:          '🌐 Preview',
    tabBrowserTitle:     'Try out web apps and static pages',
    outputPlaceholder:   'Program output appears here.',
    btnAddContext:       '＋ Send to chat',
    btnAddContextTitle:  'Attach this output to the chat as context',
    runStdinPlaceholder: 'Send to standard input (Enter)',
    runStdinSend:        'Send',

    testResultsPlaceholder:
      'Run the Gradle test task and per-method results will appear here.',
    testsRunning:        'Running tests...',
    testsNoResults:      'No test results found (compilation failed, or there were no tests).',
    testsAllPassed:      'All passed',
    testsFailedCount:    '{n} failed',
    testsCoverage:       'Coverage',
    testsExpected:       'Expected',
    testsActual:         'Actual',
    testsStackTrace:     'Stack trace',
    testsCoverageByFile: 'Coverage by file',
    testsCovLegend:      'green = executed / yellow = branch partly taken / red = not executed',
    testsCoverageOpenHint: 'Click to open this file with per-line coverage',

    btnSqlStart:         'Start DB',
    btnSqlStop:          'Stop DB',
    btnSqlRun:           'Run SQL',
    sqlHint:             'Runs the .sql open in the editor (or just the selected part)',
    sqlPlaceholder:      'Press "Start DB", then "Run SQL".',
    sqlStarting:         'Starting HSQLDB...',
    sqlStarted:          'HSQLDB is running. You can execute SQL now.',
    sqlStopped:          'HSQLDB stopped.',
    sqlNoSql:            'No SQL to run. Open a .sql file in the editor.',
    sqlRowCount:         '{n} row(s)',
    sqlUpdated:          '{n} row(s) updated.',
    sqlOk:               'Done.',
    sqlNull:             '(NULL)',

    termNoProject:       'Select a project to use the terminal.\r\n',
    termExited:          '\r\n[process exited with code {code}]\r\n',

    browserBack:         'Back',
    browserForward:      'Forward',
    browserReload:       'Reload',
    browserGo:           'Go',
    browserExternal:     'Open in external browser',
    previewNoTarget:     'Nothing to preview (check that an index.html exists, or that the app is running).',
    previewServeFailed:  'Could not serve the static page: {error}',

    errCreateFailed:     'Could not create the project: {error}',
    resetNoTemplate:     'This project was not created from an exercise template, so there is nothing to reset to.',
    confirmResetExercise:
      'Reset {name} to the state it shipped in.\nYour edits will be lost (files you added are kept).\nContinue?',
    resetDone:           'Reset done ({n} file(s) written back).',

    chatTitle:           '🤖 Chat',
    chatWelcome1:        '🤖 A coding sounding board. Using it is entirely optional.',
    chatWelcome2:        '📄 The files of the open project are sent along automatically.',
    chatWelcome3:        '🔑 Register an API key in ⚙ Settings before your first message.',
    chatInputAsk:        'Ask something... (Enter to send / Shift+Enter for a new line)',
    chatInputAgent:      'What should change in this exercise? (Enter to send / Shift+Enter for a new line)',
    btnChatClear:        '⌫ Clear',
    btnChatClearTitle:   'Erase the conversation',

    chatModeLabel:       'AI mode',
    modeAsk:             'Ask',
    modeAgent:           'Agent',
    chatModeTitle:       'Ask only reads. Agent edits files in the open exercise (it never runs them).',

    agentNoProject:      'Pick an exercise before asking the Agent.',
    agentToolList:       'Listing files',
    agentToolRead:       'Reading a file',
    agentToolWrite:      'Editing a file',
    agentToolOther:      'Working',
    agentLimit:          'Stopped after {n} steps. Ask again to continue.',
    editApplied:         'Edited',
    editUndo:            'Undo',
    editUndone:          'Undone',
    editNewFile:         'New file',
    editDiffSkipped:     '… {n} unchanged lines',
    btnSend:             '➤ Send',
    btnSendTitle:        'Send',
    btnAbort:            '⏹ Stop',
    btnAbortTitle:       'Stop the response',
    roleUser:            'You',
    roleAssistant:       '{model}',
    chatNoKey:           'No API key for {label} yet. Register one in ⚙ Settings.',
    chatError:           'Error: {error}',
    chipProject:         '📁 {n} file(s) from {name}',
    chipLog:             '📋 Run log',
    chipRemove:          'Remove this attachment',

    lspStarting:         'Starting the Java language server...',
    lspReady:            'Java language server: connected',
    lspError:            'Could not start the Java language server',

    ok:                  'OK',
  },
};

// 現在の言語 (renderer.js と共有する可変状態)
let currentLang = 'ja';

function t(key) {
  return (UI_STRINGS[currentLang] && UI_STRINGS[currentLang][key]) ||
         UI_STRINGS.ja[key] ||
         key;
}

/** {n} や {name} のプレースホルダーを置き換える */
function tf(key, params) {
  let s = t(key);
  for (const [k, v] of Object.entries(params || {})) {
    s = s.split(`{${k}}`).join(String(v));
  }
  return s;
}

function setLang(lang) {
  if (UI_STRINGS[lang]) currentLang = lang;
  document.documentElement.lang = currentLang;
}

function getLang() { return currentLang; }

function getSupportedLangs() { return Object.keys(UI_STRINGS); }

/** data-i18n* 属性を持つ要素すべてに現在の言語の文字列を当てる */
function applyI18nDom() {
  document.querySelectorAll('[data-i18n]').forEach(el => {
    el.textContent = t(el.dataset.i18n);
  });
  document.querySelectorAll('[data-i18n-title]').forEach(el => {
    el.title = t(el.dataset.i18nTitle);
  });
  document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
    el.placeholder = t(el.dataset.i18nPlaceholder);
  });
}
