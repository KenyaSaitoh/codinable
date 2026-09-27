// ═══════════════════════════════════════════════════════════
//  アプリ本体の更新 (electron-updater)
//
//  講座は別に更新する (courses.js の prepareCourseStart)。ここで扱うのは
//  プログラムの修正だけで、更新しても取り組み中の講座の版は変わらない
//
//  流れ (SP-AI Desktop Agent の src/updater.js と同じ設計)
//    1. 起動して少し経ったら黙って確認する (silent)。新しい版があれば
//       「今すぐ更新 / あとで」を聞く。勝手には入れない
//    2. 設定画面の「更新を確認」からも確認できる。こちらは最新なら「最新です」と出す
//    3. 「今すぐ更新」でダウンロードし、進捗をダイアログに出して、
//       終わったらアプリを再起動してインストールする
//
//  ダイアログは OS 標準ではなく画面側の #update-modal で描く。main は
//  表示の依頼と、押されたボタンの受け取りだけを行う (deps.prompt など)
//
//  配信先は app-config.js の getUpdateUrls().app。GitHub Releases の最新リリースに置いた
//  latest.yml と setup.exe を、generic フィードとして読む (releases/latest/download/…)
//  配信先が未設定のとき・開発実行のときは確認しない
// ═══════════════════════════════════════════════════════════

const { app } = require('electron');
const { getUpdateUrls } = require('../app-config');

let configured = false;
let checking = false;

// ダウンロードが進まないまま、どれだけ待つか
// electron-updater は応答の止まった接続を検知しないため、進捗の途絶えで判断する
const DOWNLOAD_STALL_MS = 120_000;

function feedUrl() {
  return getUpdateUrls().app;
}

/** 更新を確認できる状態か。できないときは理由も返す (設定画面に出す) */
function availability() {
  if (!feedUrl()) return { enabled: false, reason: 'not-configured' };
  if (!app.isPackaged) return { enabled: false, reason: 'dev' };
  return { enabled: true };
}

/** electron-updater の設定ファイル (app-update.yml) を userData に書き、そのパスを返す */
function writeUpdateConfig() {
  const fs = require('fs');
  const path = require('path');
  const file = path.join(app.getPath('userData'), 'app-update.yml');
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, [
    'provider: generic',
    `url: ${JSON.stringify(feedUrl())}`,
    'channel: latest',
    'updaterCacheDirName: codinable-updater',
    '',
  ].join('\n'), 'utf8');
  return file;
}

/**
 * 画面にダイアログを出して、押されたボタンの index を返す
 * deps.prompt が無い (画面が無い) ときは取り消し扱いにする
 */
async function ask(deps, spec) {
  if (typeof deps.prompt !== 'function') return spec.cancelIndex || 0;
  try { return Number(await deps.prompt(spec)) || 0; }
  catch { return spec.cancelIndex || 0; }
}

/**
 * 更新を確認し、あれば受講者に聞いてからダウンロード・インストールまで行う
 *
 *   deps.t(key, vars)    : 文言 (画面の表示言語に合わせる)
 *   deps.prompt(spec)    : ダイアログを出し、押されたボタンの index を返す
 *   deps.showProgress(s) : 応答を待たない進捗ダイアログを出す
 *   deps.progress(p)     : ダウンロードの進捗 (0-100)
 *   deps.dismiss()       : 出しているダイアログを閉じる
 *   opts.silent          : true なら「最新です」を出さない (起動時の確認)
 */
async function checkForUpdates(deps, opts = {}) {
  const silent = opts.silent !== false;
  const t = deps.t;
  const current = app.getVersion();

  const state = availability();
  if (!state.enabled) return { ok: true, status: 'skipped', reason: state.reason, current };
  if (checking) return { ok: true, status: 'skipped', reason: 'checking', current };

  checking = true;
  // ダウンロードを始めたあとの失敗は、起動時の確認でも必ず知らせる
  let downloadStarted = false;
  try {
    const { autoUpdater, CancellationToken } = require('electron-updater');
    if (!configured) {
      autoUpdater.logger = {
        info: m => console.log('[updater]', m),
        warn: m => console.warn('[updater]', m),
        error: m => console.error('[updater]', m),
        debug: () => {},
      };
      autoUpdater.autoDownload = false;
      autoUpdater.autoInstallOnAppQuit = true;
      // 差分 (blockmap) ダウンロードは使わない。多数の Range 要求に分かれるため、
      // 途中の 1 本が止まると終盤で滞留することがある
      autoUpdater.disableDifferentialDownload = true;
      autoUpdater.setFeedURL({
        provider: 'generic',
        url: feedUrl(),
        useMultipleRangeRequest: false,
        channel: 'latest',
      });
      // ダウンロードの置き場の名前などは app-update.yml から読まれる。electron-builder は
      // NSIS のときしかこれを作らず、配信先を実行時に差し替えたときとも食い違うため、
      // 自分で書いたものを読ませる
      autoUpdater.updateConfigPath = writeUpdateConfig();
      configured = true;
    }

    const result = await autoUpdater.checkForUpdates();
    const info = result && result.updateInfo;
    if (!result?.isUpdateAvailable || !info?.version || info.version === current) {
      if (!silent) {
        await ask(deps, {
          kind: 'info',
          title: t('updTitleCheck'),
          message: t('updLatest', { version: current }),
          buttons: [t('updOk')],
        });
      }
      return { ok: true, status: 'latest', current };
    }

    const answer = await ask(deps, {
      kind: 'confirm',
      title: t('updTitleAvailable'),
      message: t('updAvailable', { version: info.version, current }),
      detail: t('updAvailableDetail'),
      buttons: [t('updNow'), t('updLater')],
      defaultIndex: 0,
      cancelIndex: 1,
    });
    if (answer !== 0) return { ok: true, status: 'postponed', version: info.version, current };

    const cancellationToken = new CancellationToken();
    let lastProgressAt = Date.now();
    let stalled = false;
    const onProgress = p => {
      lastProgressAt = Date.now();
      deps.progress?.(p && p.percent ? p.percent : 0);
    };
    autoUpdater.on('download-progress', onProgress);
    const stallTimer = setInterval(() => {
      if (Date.now() - lastProgressAt < DOWNLOAD_STALL_MS) return;
      stalled = true;
      try { cancellationToken.cancel(); } catch { /* すでに取り消している */ }
    }, 5000);
    // 進捗ダイアログはボタンを持たず、応答を待たない (終わったら閉じる)
    deps.showProgress?.({
      kind: 'progress',
      title: t('updTitleDownloading'),
      message: t('updDownloading', { version: info.version }),
      detail: t('updDownloadingDetail'),
    });
    downloadStarted = true;
    try {
      await autoUpdater.downloadUpdate(cancellationToken);
    } catch (err) {
      if (stalled) throw Object.assign(new Error('download stalled'), { text: t('updStalled') });
      throw err;
    } finally {
      clearInterval(stallTimer);
      autoUpdater.removeListener('download-progress', onProgress);
    }
    // 開いているファイルの書き出しは画面側が before-install で済ませる
    await deps.beforeInstall?.();
    // 子プロセスの後片づけは main.js の before-quit が行う
    setImmediate(() => autoUpdater.quitAndInstall(false, true));
    return { ok: true, status: 'updated', version: info.version, current };
  } catch (err) {
    // まだ 1 度も公開していない (latest.yml が無い) ときは「最新」として扱う
    if (!downloadStarted && /\b404\b/.test(String(err.message))) {
      if (!silent) {
        await ask(deps, {
          kind: 'info',
          title: t('updTitleCheck'),
          message: t('updLatest', { version: current }),
          buttons: [t('updOk')],
        });
      }
      return { ok: true, status: 'latest', current };
    }
    console.error('[updater] failed:', err.message);
    deps.dismiss?.();
    if (!silent || downloadStarted) {
      await ask(deps, {
        kind: 'error',
        title: t(downloadStarted ? 'updTitleInstallFailed' : 'updTitleCheckFailed'),
        message: err.text || err.message || String(err),
        buttons: [t('updOk')],
      });
    }
    return { ok: false, status: 'error', error: err.message || String(err), current };
  } finally {
    checking = false;
  }
}

module.exports = { availability, checkForUpdates };
