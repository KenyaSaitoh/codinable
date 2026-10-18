package pro.kensait.jakartaee.employee.service;

/** 更新競合を表す例外 */
public class ConflictException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ConflictException() {
        super("ほかの人が先に更新しました。読み込み直してください");
    }
}
