package pro.kensait.spring.employee.service;

/*
 * 社員情報の同時更新を検出した場合に発生する例外
 */
public class ConflictException extends RuntimeException {
    // 競合の初期化
    public ConflictException() {
        super("ほかの人が先に更新しました読み込み直してください");
    }
}
