package pro.kensait.jakartaee.employee.service;

/** 社員が存在しないことを表す例外 */
public class EmployeeNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EmployeeNotFoundException() {
        super("該当する社員が見つかりません");
    }
}
