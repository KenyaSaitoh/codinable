package pro.kensait.jakartaee.employee.service;

/** 月給の検索範囲が不正であることを表す例外 */
public class RangeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RangeException() {
        super("月給の範囲が正しくありません");
    }
}
