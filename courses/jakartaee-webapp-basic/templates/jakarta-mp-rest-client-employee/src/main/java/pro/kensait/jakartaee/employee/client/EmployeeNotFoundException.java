package pro.kensait.jakartaee.employee.client;

/** 呼び出し先の API が 404 を返したことを表す（EmployeeNotFoundMapper が作る） */
public class EmployeeNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
