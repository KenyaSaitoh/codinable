package pro.kensait.leafbooks.api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

/*
 * registerに使用するデータ
 */
public record RegisterRequest(
        @NotBlank(message = "顧客名を入力してください")
        String customerName,
        
        @NotBlank(message = "メールアドレスを入力してください")
        @Email(message = "メールアドレスの形式が正しくありません")
        String email,
        
        @NotBlank(message = "パスワードを入力してください")
        String password,
        
        @NotNull(message = "誕生日を入力してください")
        @Past(message = "誕生日は過去の日付を入力してください")
        LocalDate birthday,
        
        @NotBlank(message = "住所を入力してください")
        String address
) {}

