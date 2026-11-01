package pro.kensait.spring.users;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * Web API (REST) のエンドポイント
 *
 * サーバーサイド MVC (spring-mvc-calc) との違いは、返すものが HTML ではなく
 * JSON であることと、状態を URL とメソッドで表すこと
 *
 * @RestController は @Controller + @ResponseBody に相当し、
 * 戻り値のオブジェクトを Jackson が JSON に変換して本文にする
 *
 * ▶ アプリ実行で動かしたまま、curl/ のスクリプトを順に開いて
 * エディタの右上の ▶ 実行で送ると確かめられる:
 *   curl/01_list_users.sh   一覧取得 (GET /api/users)
 *   curl/02_get_user.sh     1 件取得 (GET /api/users/1)
 *   curl/03_create_user.sh  登録 (POST /api/users、201 と Location)
 *   curl/04_delete_user.sh  削除 (DELETE /api/users/1、204) と削除後の 404
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** 一覧取得: 200 OK */
    @GetMapping
    public List<User> list() {
        return userService.findAll();
    }

    /** 1 件取得: 200 OK / 見つからなければ 404 */
    @GetMapping("/{id}")
    public User get(@PathVariable int id) {
        return userService.findById(id);
    }

    /**
     * 登録: 201 Created
     *
     * 作ったものの場所を Location ヘッダーで返すのが REST の作法
     * 戻り値の型を ResponseEntity にすると、状態コードとヘッダーを自分で決められる
     */
    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        User created = userService.create(user);
        return ResponseEntity
                .created(URI.create("/api/users/" + created.id()))
                .body(created);
    }

    /** 置き換え: 200 OK / 見つからなければ 404 */
    @PutMapping("/{id}")
    public User update(@PathVariable int id, @Valid @RequestBody User user) {
        return userService.update(id, user);
    }

    /** 削除: 204 No Content (返す本文が無いことを表す) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
