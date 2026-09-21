import { useState, type FormEvent } from 'react';

export type Profile = {
  name: string;
  email: string;
  role: string;
};

type ProfileFormProps = {
  onSubmit: (profile: Profile) => void;
};

const emptyProfile: Profile = { name: '', email: '', role: '開発' };

const ProfileForm = ({ onSubmit }: ProfileFormProps) => {
  const [profile, setProfile] = useState(emptyProfile);

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault(); // ページ遷移は行わず、Reactの処理だけを実行する。
    onSubmit(profile);
  };

  return (
    <form onSubmit={handleSubmit}>
      <label>
        名前
        <input
          required
          value={profile.name}
          onChange={(event) => setProfile({ ...profile, name: event.target.value })}
        />
      </label>
      <label>
        メール
        <input
          required
          type="email"
          value={profile.email}
          onChange={(event) => setProfile({ ...profile, email: event.target.value })}
        />
      </label>
      <label>
        担当
        <select
          value={profile.role}
          onChange={(event) => setProfile({ ...profile, role: event.target.value })}
        >
          <option>設計</option>
          <option>開発</option>
          <option>テスト</option>
        </select>
      </label>
      <div className="buttons">
        <button type="submit">確認する</button>
        <button type="button" className="secondary" onClick={() => setProfile(emptyProfile)}>
          クリア
        </button>
      </div>
    </form>
  );
};

export default ProfileForm;
