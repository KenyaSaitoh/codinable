package pro.kensait.leafbooks.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 社員ログインリクエストDTO
 */
public record EmployeeLoginRequest(
    @NotBlank(message = "社員コードは必須です")
    String employeeCode,

    @NotBlank(message = "パスワードは必須です")
    String password
) {
}
