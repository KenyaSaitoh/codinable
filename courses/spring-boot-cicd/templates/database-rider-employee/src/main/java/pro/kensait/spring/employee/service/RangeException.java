package pro.kensait.spring.employee.service;

/*
 * 月給の検索範囲が不正な場合に発生する例外
 */
public class RangeException extends RuntimeException {
    // 範囲の初期化
    public RangeException() {
        super("月給の範囲が正しくありません");
    }
}
