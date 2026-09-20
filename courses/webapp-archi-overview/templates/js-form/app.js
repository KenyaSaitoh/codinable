// フォームは、DOM 操作とイベント処理が一通り登場する題材である。
//
// 入力欄の値は value、チェック状態は checked で読み書きする。
// 送信は submit イベントで受け取り、event.preventDefault() で
// Web ブラウザ既定のページ遷移を止めてから自分で処理する。

const form            = document.getElementById('registration-form');
const email           = document.getElementById('email');
const password        = document.getElementById('password');
const confirmPassword = document.getElementById('confirm-password');
const errorMessage    = document.getElementById('error-message');
const result          = document.getElementById('result');

// ── 入力中のリアルタイム検証 ─────────────────────────
// input は 1 文字ごとに発生する。入力を終える前に知らせられるので、
// ユーザーは「送信して初めて怒られる」ことがなくなる。
password.addEventListener('input', () => {
  const hint = document.getElementById('password-hint');
  if (8 <= password.value.length) {
    password.style.borderColor = '#1a7f37';
    hint.textContent = 'この長さで足りる';
  } else {
    password.style.borderColor = '#bf8700';
    hint.textContent = `あと ${8 - password.value.length} 文字`;
  }
});

email.addEventListener('input', () => {
  // HTML の type="email" でも形式は検証されるが、
  // 「打っている途中に知らせる」のは JavaScript の仕事である
  const hint = document.getElementById('email-hint');
  hint.textContent = email.value.includes('@') ? '' : '@ が入っていない';
});

// ── 送信時の検証 ─────────────────────────────────────
form.addEventListener('submit', event => {
  // 既定の動作（ページ遷移）を止める。これを忘れると画面が再読み込みされる
  event.preventDefault();
  errorMessage.textContent = '';

  // HTML の検証属性では書けない「2 つの欄を見比べる」検証
  if (password.value !== confirmPassword.value) {
    errorMessage.textContent = 'パスワードが一致しない';
    confirmPassword.style.borderColor = '#cf222e';
    confirmPassword.focus();
    return;
  }
  confirmPassword.style.borderColor = '';

  if (password.value.length < 8) {
    errorMessage.textContent = 'パスワードは 8 文字以上で入力する';
    return;
  }

  // ── 値の取り出し ──────────────────────────────────
  // FormData はフォームの入力値をまとめて集める仕組み。
  // name 属性が付いていて disabled でない欄が対象になる。
  const formData = new FormData(form);

  // 同じ name が複数ある項目（チェックボックス）は getAll でまとめて取る
  const skills = formData.getAll('skills');

  // 1 つずつ取り出すこともできる
  const entries = {
    name:       formData.get('name'),
    email:      formData.get('email'),
    department: formData.get('department'),
    // ラジオボタンは「選ばれているもの」を CSS セレクタで指す。
    // ?. は左辺が null のときにエラーにせず undefined を返す書き方（オプショナルチェイニング）
    work:       document.querySelector('input[name="work"]:checked')?.value,
    skills:     skills.length ? skills.join(' / ') : '（なし）',
    // チェックボックスの状態は checked で読む（value ではない）
    agree:      document.getElementById('agree').checked,
    // パスワードは画面に出さない
    password:   '*'.repeat(password.value.length),
  };

  result.textContent = Object.entries(entries)
    .map(([key, value]) => `${key} = ${value}`)
    .join('\n');

  // ここでサーバーへ送るときは、JSON にして fetch で POST する。
  // その形は js-fetch の演習で扱う。
  //
  //   await fetch('/api/users', {
  //     method: 'POST',
  //     headers: { 'Content-Type': 'application/json' },
  //     body: JSON.stringify(Object.fromEntries(formData)),
  //   });

  form.reset();                       // 入力欄を空に戻す
  password.style.borderColor = '';
  document.getElementById('password-hint').textContent = '';
  document.getElementById('email-hint').textContent    = '';
  errorMessage.textContent = '';
});
