package pro.kensait.leafbooks.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/*
 * ログインに使用するデータ
 */
public record LoginRequest(
        @NotBlank(message = "メールアドレスを入力してください")
        @Email(message = "メールアドレスの形式が正しくありません")
        String email,
        
        @NotBlank(message = "パスワードを入力してください")
        String password
) {}

