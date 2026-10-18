package pro.kensait.jakartaee.employee.graphql.service;

/*
 * 社員が存在しないことを表す業務の例外
 * チェック例外なので、メッセージがそのまま GraphQL の errors の message に入る
 */
public class EmployeeNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
