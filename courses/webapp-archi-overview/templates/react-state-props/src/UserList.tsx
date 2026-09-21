export type User = { id: number; name: string };

type UserListProps = {
  users: User[];
  onDelete: (id: number) => void;
};

// 子はPropsを表示し、変更したいときは親から受け取った関数を呼ぶ
const UserList = ({ users, onDelete }: UserListProps) => {
  if (users.length === 0) return <p className="empty">メンバーはいません。</p>;

  return (
    <ul>
      {users.map((user) => (
        <li key={user.id}>
          <span>{user.name}</span>
          <button onClick={() => onDelete(user.id)}>削除</button>
        </li>
      ))}
    </ul>
  );
};

export default UserList;
