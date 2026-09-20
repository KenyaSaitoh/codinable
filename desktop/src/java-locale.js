const JAVA_LOCALES = Object.freeze({
  ja:      Object.freeze({ language: 'ja', country: 'JP' }),
  en:      Object.freeze({ language: 'en', country: 'US' }),
  fr:      Object.freeze({ language: 'fr', country: 'FR' }),
  de:      Object.freeze({ language: 'de', country: 'DE' }),
  es:      Object.freeze({ language: 'es', country: 'ES' }),
  'pt-BR': Object.freeze({ language: 'pt', country: 'BR' }),
  zh:      Object.freeze({ language: 'zh', country: 'CN' }),
  ko:      Object.freeze({ language: 'ko', country: 'KR' }),
  id:      Object.freeze({ language: 'id', country: 'ID' }),
  it:      Object.freeze({ language: 'it', country: 'IT' }),
});

function resolveJavaLocale(uiLang) {
  return Object.prototype.hasOwnProperty.call(JAVA_LOCALES, uiLang)
    ? JAVA_LOCALES[uiLang]
    : JAVA_LOCALES.ja;
}

function getJavaRuntimeOptions(uiLang) {
  const { language, country } = resolveJavaLocale(uiLang);
  return [
    '-Dfile.encoding=UTF-8',
    '-Dstdout.encoding=UTF-8',
    '-Dstderr.encoding=UTF-8',
    `-Duser.language=${language}`,
    `-Duser.country=${country}`,
  ];
}

function getJavacRuntimeOptions(uiLang) {
  return getJavaRuntimeOptions(uiLang).map(option => `-J${option}`);
}

function appendJvmOptions(existing, options) {
  return [String(existing || '').trim(), ...options]
    .filter(Boolean)
    .join(' ');
}

/**
 * JAVA_TOOL_OPTIONS reaches Java processes forked by Gradle (bootRun/tests).
 * GRADLE_OPTS also makes locale-sensitive immutable properties part of the
 * Gradle Daemon compatibility request, preventing reuse of another locale's
 * already-running daemon.
 */
function applyJavaLocaleToEnv(baseEnv, uiLang, { gradle = false } = {}) {
  const env = { ...baseEnv };
  const options = getJavaRuntimeOptions(uiLang);
  env.JAVA_TOOL_OPTIONS = appendJvmOptions(env.JAVA_TOOL_OPTIONS, options);
  if (gradle) env.GRADLE_OPTS = appendJvmOptions(env.GRADLE_OPTS, options);
  return env;
}

module.exports = {
  JAVA_LOCALES,
  resolveJavaLocale,
  getJavaRuntimeOptions,
  getJavacRuntimeOptions,
  applyJavaLocaleToEnv,
};
