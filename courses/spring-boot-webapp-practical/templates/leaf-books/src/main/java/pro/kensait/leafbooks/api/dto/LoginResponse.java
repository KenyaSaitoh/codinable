package pro.kensait.leafbooks.api.dto;

import java.time.LocalDate;

/*
 * ログインに使用するデータ
 */
public record LoginResponse(
        Integer customerId,
        String customerName,
        String email,
        LocalDate birthday,
        String address
) {}

