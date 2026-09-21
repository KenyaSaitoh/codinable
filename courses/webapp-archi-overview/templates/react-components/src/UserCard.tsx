export type User = {
  id: number;
  name: string;
  email: string;
  role: string;
};

// Props は親コンポーネントから渡される読み取り専用の入力。
const UserCard = ({ name, email, role }: User) => (
  <article className="user-card">
    <span className="role">{role}</span>
    <h2>{name}</h2>
    <a href={`mailto:${email}`}>{email}</a>
  </article>
);

export default UserCard;
