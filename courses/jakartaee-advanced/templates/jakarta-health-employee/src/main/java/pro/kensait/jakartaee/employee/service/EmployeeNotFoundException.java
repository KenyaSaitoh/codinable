package pro.kensait.jakartaee.employee.service;

/*
 * 社員が存在しないことを表す例外（EmployeeNotFoundExceptionMapper が 404 にする）
 */
public class EmployeeNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
