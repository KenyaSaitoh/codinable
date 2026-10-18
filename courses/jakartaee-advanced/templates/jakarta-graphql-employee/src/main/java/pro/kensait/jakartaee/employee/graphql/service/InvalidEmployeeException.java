package pro.kensait.jakartaee.employee.graphql.service;

/*
 * 入力が業務上正しくないことを表す例外（チェック例外。メッセージが errors に入る）
 */
public class InvalidEmployeeException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidEmployeeException(String message) {
        super(message);
    }
}
