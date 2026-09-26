package pro.kensait.leafbooks.external;

import java.time.LocalDate;

/*
 * 顧客変換先を表すレコード
 */
public record CustomerTO (
        // 顧客ID
        Integer customerId,
        // 顧客名
         String customerName,
        // パスワード
         String password,
        // メールアドレス
         String email,
        // 生年月日
         LocalDate birthday,
         // 住所
         String address) {
}

