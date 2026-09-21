package pro.kensait.spring.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
 * API がやりとりするデータ
 *
 * record にすると、Jackson がそのまま JSON に変換してくれる
 * (フィールド名がそのまま JSON のキーになる)
 */
public record User(
        Integer id,

        @NotBlank
        @Size(max = 30)
        String name,

        @NotBlank
        @Email
        String email) {

    /** id を採番した新しい User を返す (record は不変なので作り直す) */
    public User withId(Integer newId) {
        return new User(newId, name, email);
    }
}
