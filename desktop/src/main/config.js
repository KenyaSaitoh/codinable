// ═══════════════════════════════════════════════════════════
//  設定の永続化
//
//  保存先: <userData>/codinable-config.json (JSON)
//  API キーだけは Electron の safeStorage (OS のキーチェーン由来の鍵) で
//  暗号化して <field>Encrypted に入れる。safeStorage が使えない環境
//  (Linux の一部) では平文フィールドにフォールバックする
// ═══════════════════════════════════════════════════════════

const fs   = require('fs');
const os   = require('os');
const path = require('path');
const { app, safeStorage } = require('electron');

const { PRODUCT, API_KEY_FIELDS, DEFAULT_LLM_ID } = require('../app-config');

const CONFIG_FILE = 'codinable-config.json';

function getConfigPath() {
  return path.join(app.getPath('userData'), CONFIG_FILE);
}

function loadConfig() {
  try {
    return JSON.parse(fs.readFileSync(getConfigPath(), 'utf8'));
  } catch {
    return {};
  }
}

function saveConfig(config) {
  try {
    // 初回起動や userData を差し替えたときは、置き場がまだ無い
    fs.mkdirSync(path.dirname(getConfigPath()), { recursive: true });
    fs.writeFileSync(getConfigPath(), JSON.stringify(config, null, 2), 'utf8');
    return true;
  } catch (err) {
    console.error('[config] save failed:', err.message);
    return false;
  }
}

/** 設定のキーひとつを読み書きする小道具 */
function patchConfig(patch) {
  const config = loadConfig();
  Object.assign(config, patch);
  saveConfig(config);
  return config;
}

// ── API キー ───────────────────────────────────────────────

function canEncrypt() {
  try { return safeStorage.isEncryptionAvailable(); } catch { return false; }
}

/** API キーを保存する (field は app-config.js の keyField) */
function saveApiKey(field, key) {
  if (!API_KEY_FIELDS.includes(field)) return { ok: false, error: `unknown key field: ${field}` };
  const config = loadConfig();
  const value  = String(key ?? '').trim();

  delete config[field];
  delete config[`${field}Encrypted`];

  if (value) {
    if (canEncrypt()) {
      config[`${field}Encrypted`] = safeStorage.encryptString(value).toString('base64');
    } else {
      config[field] = value;
    }
  }
  saveConfig(config);
  return { ok: true, configured: !!value };
}

/** API キーを復号して返す (未設定なら '') */
function loadApiKey(field, config = null) {
  const cfg = config || loadConfig();
  const encrypted = cfg[`${field}Encrypted`];
  if (encrypted) {
    try {
      return safeStorage.decryptString(Buffer.from(encrypted, 'base64'));
    } catch (err) {
      console.warn(`[config] ${field} decrypt failed:`, err.message);
    }
  }
  return String(cfg[field] || '');
}

/** 設定パネル表示用: どのキーが登録済みか (キー本体は renderer へ渡さない) */
function getApiKeyStatus() {
  const config = loadConfig();
  return Object.fromEntries(
    API_KEY_FIELDS.map(field => [field, !!loadApiKey(field, config)]),
  );
}

/** LLM プロバイダへ渡す資格情報 (復号済み) */
function loadApiKeys() {
  const config = loadConfig();
  return Object.fromEntries(
    API_KEY_FIELDS.map(field => [field, loadApiKey(field, config)]),
  );
}

// ── ワークスペース / 言語 / 選択中モデル ───────────────────

function getWorkspaceRoot() {
  return loadConfig().workspaceRoot || path.join(os.homedir(), PRODUCT.workspaceDirName);
}

function setWorkspaceRoot(dir) {
  patchConfig({ workspaceRoot: dir });
  return { ok: true, root: getWorkspaceRoot() };
}

/**
 * インストーラが選んだ表示言語を読む (無ければ null)
 *
 * NSIS のカスタムページ (assets/installer.nsh) が、選ばれた言語を
 * インストール先の default-lang.txt に書き出している。設定ファイルに
 * まだ言語が無いとき、つまり初回起動時だけこれを見る
 */
function readInstallerLang() {
  try {
    const file = path.join(path.dirname(app.getPath('exe')), 'default-lang.txt');
    const lang = fs.readFileSync(file, 'utf8').trim();
    if (PRODUCT.languages.includes(lang)) return lang;
  } catch { /* 開発実行時やファイルが無いときはここに来る */ }
  return null;
}

function getUiLang() {
  const saved = String(loadConfig().uiLang || '').trim();
  if (PRODUCT.languages.includes(saved)) return saved;

  // 初回起動時: インストーラでの選択 → OS の表示言語 → 先頭 (ja) の順で決める
  const installer = readInstallerLang();
  if (installer) return installer;

  const locale = String(app.getLocale() || '').toLowerCase();
  const matched = PRODUCT.languages.find(lang => locale.startsWith(lang));
  return matched || PRODUCT.languages[0];
}

function setUiLang(lang) {
  if (PRODUCT.languages.includes(lang)) patchConfig({ uiLang: lang });
  return { ok: true, lang: getUiLang() };
}

function getLlmSelection() {
  const config = loadConfig();
  return {
    modelId:  config.llmModelId || DEFAULT_LLM_ID,
    // 空文字なら app-config.js の既定モデル ID を使う
    override: String(config.llmModelOverride || '').trim(),
    // 'ask' (読むだけ) / 'agent' (書き換えと実行までする)
    chatMode: config.chatMode === 'agent' ? 'agent' : 'ask',
  };
}

function setLlmSelection({ modelId, override, chatMode }) {
  const patch = {};
  if (modelId !== undefined) patch.llmModelId = modelId;
  if (override !== undefined) patch.llmModelOverride = String(override || '').trim();
  if (chatMode !== undefined) patch.chatMode = chatMode === 'agent' ? 'agent' : 'ask';
  patchConfig(patch);
  return getLlmSelection();
}

module.exports = {
  getConfigPath,
  loadConfig,
  saveConfig,
  patchConfig,
  saveApiKey,
  loadApiKey,
  loadApiKeys,
  getApiKeyStatus,
  getWorkspaceRoot,
  setWorkspaceRoot,
  getUiLang,
  setUiLang,
  getLlmSelection,
  setLlmSelection,
};
