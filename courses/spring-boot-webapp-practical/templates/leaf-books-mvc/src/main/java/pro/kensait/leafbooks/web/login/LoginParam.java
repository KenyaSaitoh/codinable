package pro.kensait.leafbooks.web.login;

import jakarta.validation.constraints.NotEmpty;

/*
 * ログインパラメータに使用するデータ
 */
public record LoginParam(
        // メールアドレス
        @NotEmpty
        String email,
        // パスワード
        @NotEmpty
        String password) {
}
