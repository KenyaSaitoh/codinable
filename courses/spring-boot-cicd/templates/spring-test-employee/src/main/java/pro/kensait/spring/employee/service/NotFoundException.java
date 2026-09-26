package pro.kensait.spring.employee.service;
/** 社員が存在しないことを表す例外 */
public class NotFoundException extends RuntimeException {
    // 非検出の初期化
    public NotFoundException() {
        super("該当する社員が見つかりません");
    }
}
