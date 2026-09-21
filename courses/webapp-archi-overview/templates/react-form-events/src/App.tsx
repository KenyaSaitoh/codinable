import { useState } from 'react';
import ProfileForm, { type Profile } from './ProfileForm';

const App = () => {
  const [submitted, setSubmitted] = useState<Profile | null>(null);

  return (
    <main>
      <h1>制御されたフォーム</h1>
      <p>各inputの表示値とReactのStateを、onChangeで常に同期します。</p>
      <div className="layout">
        <section>
          <h2>入力</h2>
          <ProfileForm onSubmit={setSubmitted} />
        </section>
        <section aria-live="polite">
          <h2>送信イベントの結果</h2>
          {submitted ? (
            <dl>
              <dt>名前</dt><dd>{submitted.name}</dd>
              <dt>メール</dt><dd>{submitted.email}</dd>
              <dt>担当</dt><dd>{submitted.role}</dd>
            </dl>
          ) : (
            <p className="empty">フォームを送信すると、Stateからここを再描画します。</p>
          )}
        </section>
      </div>
    </main>
  );
};

export default App;
