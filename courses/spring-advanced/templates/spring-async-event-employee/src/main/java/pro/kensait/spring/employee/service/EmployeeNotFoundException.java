package pro.kensait.spring.employee.service;

/*
 * 指定した社員が存在しないことを表す例外
 */
public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(Integer employeeId) {
        super("社員が存在しません: " + employeeId);
    }
}
