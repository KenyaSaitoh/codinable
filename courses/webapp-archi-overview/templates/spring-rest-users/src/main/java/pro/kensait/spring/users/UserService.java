package pro.kensait.spring.users;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

/*
 * 利用者データの出し入れ
 *
 * 本来はデータベースを使うところだが、ここでは Map をデータベースの代わりにする
 * (アプリを止めると消える)。HTTP のことを知らない層であることが大事
 */
@Service
public class UserService {

    private final Map<Integer, User> store = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(0);

    public UserService() {
        create(new User(null, "Alice", "alice@example.com"));
        create(new User(null, "Bob", "bob@example.com"));
    }

    public List<User> findAll() {
        // id の順に並べて返す (Map は順序を保証しない)
        List<User> users = new ArrayList<>(store.values());
        users.sort((a, b) -> Integer.compare(a.id(), b.id()));
        return users;
    }

    public User findById(int id) {
        User user = store.get(id);
        if (user == null) {
            throw new UserNotFoundException(id);
        }
        return user;
    }

    public User create(User user) {
        User created = user.withId(sequence.incrementAndGet());
        store.put(created.id(), created);
        return created;
    }

    public User update(int id, User user) {
        // 無ければ例外。「置き換え」なので、有ることを先に確かめる
        findById(id);
        User updated = user.withId(id);
        store.put(id, updated);
        return updated;
    }

    public void delete(int id) {
        findById(id);
        store.remove(id);
    }
}
