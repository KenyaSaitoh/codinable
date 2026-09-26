package pro.kensait.spring.employee.service;

/*
 * 対象の社員が存在しない場合に発生する例外
 */
public class NotFoundException extends RuntimeException {
    // 非検出の初期化
    public NotFoundException() {
        super("該当する社員が見つかりません");
    }
}
