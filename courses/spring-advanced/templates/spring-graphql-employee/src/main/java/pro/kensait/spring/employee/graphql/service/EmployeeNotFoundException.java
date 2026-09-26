package pro.kensait.spring.employee.graphql.service;

/*
 * 社員が存在しないことを表す例外クラス
 */
public class EmployeeNotFoundException extends RuntimeException {
    // コンストラクタ
    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
