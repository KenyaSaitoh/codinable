package pro.kensait.spring.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/*
 * UserService の単体テスト。
 *
 * Spring を起動しないので速い。ビジネスロジックの確認はここで済ませ、
 * HTTP のやりとりは UserControllerTest に任せる、という役割分担にする。
 *
 * 実行対象で「Gradle: test」を選ぶと、テスト結果タブに一覧とカバレッジが出る。
 */
class UserServiceTest {

    private UserService userService;

    // 各テストの前に作り直す。テストが互いの結果に影響しないようにするため
    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    @Test
    @DisplayName("初期データが 2 件ある")
    void testFindAll() {
        assertEquals(2, userService.findAll().size());
    }

    @Test
    @DisplayName("id で 1 件取得できる")
    void testFindById() {
        assertEquals("Alice", userService.findById(1).name());
    }

    @Test
    @DisplayName("存在しない id は例外になる")
    void testFindByIdNotFound() {
        assertThrows(UserNotFoundException.class, () -> userService.findById(999));
    }

    @Test
    @DisplayName("登録すると id が採番される")
    void testCreate() {
        User created = userService.create(new User(null, "Carol", "carol@example.com"));
        assertEquals(3, created.id());
        assertEquals(3, userService.findAll().size());
    }

    @Test
    @DisplayName("更新すると内容が置き換わる")
    void testUpdate() {
        User updated = userService.update(1, new User(null, "Alice2", "alice2@example.com"));
        assertEquals(1, updated.id());
        assertEquals("Alice2", userService.findById(1).name());
    }

    @Test
    @DisplayName("削除すると件数が減る")
    void testDelete() {
        userService.delete(1);
        assertEquals(1, userService.findAll().size());
        assertThrows(UserNotFoundException.class, () -> userService.findById(1));
    }
}
