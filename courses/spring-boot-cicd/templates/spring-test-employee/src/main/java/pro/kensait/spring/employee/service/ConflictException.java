package pro.kensait.spring.employee.service;
/** 更新競合を表す例外 */
public class ConflictException extends RuntimeException {
    // 競合の初期化
    public ConflictException() {
        super("ほかの人が先に更新しました読み込み直してください");
    }
}
