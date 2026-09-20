// ═══════════════════════════════════════════════════════════
//  electron-builder 設定 (Codinable)
//
//    npm run pack    → dist-installer/win-unpacked (署名なし・動作確認用)
//    npm run build   → dist-installer/Codinable-setup-<version>.exe
//
//  Chatinable はエディション (収録コース・同梱ランタイム・言語) ごとに
//  設定を組み立てていたが、Codinable は 1 構成しか作らないため、
//  ここに直接書いている。
//
//  electron-builder の extraMetadata は使わないこと。ソースの package.json を
//  上書きし、scripts / devDependencies が消えてしまう副作用がある。
// ═══════════════════════════════════════════════════════════

const { PRODUCT } = require('../src/app-config');

module.exports = {
  appId:       PRODUCT.appId,
  productName: PRODUCT.productName,
  copyright:   `Copyright © 2025-2026 ${PRODUCT.displayName}`,
  asar:        true,

  files: [
    'src/**',
    'node_modules/**',
    'package.json',
    // コースパック (course.yaml + 雛形) は asar の中に入れる。
    // 実体が 1 ファイルになり、NSIS の展開と Defender のスキャンが速い。
    // 読むだけ (実行・書き込みをしない) なので asar 透過読み込みで足りる。
    // 参照側は src/main/courses.js の getCoursesDir()。
    { from: '../courses', to: 'courses',
      filter: ['**/*', '!**/.gradle/**', '!**/build/**', '!**/bin/**', '!**/node_modules/**'] },
  ],

  extraResources: [
    // ランタイムは子プロセスとして実行するため asar には入れられない
    // (java.exe / node.exe / python.exe は実ファイルである必要がある)
    { from: '../runtime/java',             to: 'runtime/java',   filter: ['**/*'] },
    { from: '../runtime/node',             to: 'runtime/node',   filter: ['**/*'] },
    { from: '../runtime/python',           to: 'runtime/python', filter: ['**/*'] },
    { from: '../runtime/bash',             to: 'runtime/bash',   filter: ['**/*'] },
    { from: '../hsqldb',                   to: 'hsqldb',         filter: ['*.jar'] },
    { from: '../resources/gradle-wrapper', to: 'gradle-wrapper', filter: ['**/*'] },
    // Java の言語サーバー (Eclipse JDT LS)。補完・定義ジャンプ・診断に使う
    { from: '../resources/jdtls',          to: 'jdtls',          filter: ['**/*'] },
  ],

  publish: null,

  directories: {
    output:         'dist-installer',
    buildResources: 'assets',
  },

  win: {
    target: [{ target: 'nsis', arch: ['x64'] }],
    requestedExecutionLevel: 'asInvoker',
    icon: 'assets/icon.ico',
  },

  nsis: {
    oneClick:   false,
    perMachine: false,
    // 日本語と英語の 2 言語。インストーラの 1 ページ目で切り替え、
    // 選ばれた言語は default-lang.txt 経由でアプリの初期表示言語にもなる
    // (assets/installer.nsh と src/main/config.js の readInstallerLang)。
    //
    // multiLanguageInstaller を false にすると electron-builder は
    // installerLanguages を無視して英語固定のインストーラを作るため、
    // 2 言語でも true のままにする (app-builder-lib の LangConfigurator)。
    multiLanguageInstaller: true,
    // 言語選択は assets/installer.nsh の自前ページで行うので、
    // NSIS 標準の言語選択ダイアログは出さない
    displayLanguageSelector: false,
    installerLanguages:      ['en_US', 'ja_JP'],
    include:                 'assets/installer.nsh',
    allowToChangeInstallationDirectory: true,
    createDesktopShortcut:   true,
    createStartMenuShortcut: true,
    shortcutName:            PRODUCT.displayName,
    uninstallDisplayName:    PRODUCT.displayName,
    deleteAppDataOnUninstall: false,
    license:                 'assets/license.txt',
    artifactName:            `${PRODUCT.productName}-setup-\${version}.\${ext}`,
    installerIcon:           'assets/icon.ico',
    uninstallerIcon:         'assets/icon.ico',
    installerHeader:         'assets/installerHeader.bmp',
    installerSidebar:        'assets/installerSidebar.bmp',
  },
};
