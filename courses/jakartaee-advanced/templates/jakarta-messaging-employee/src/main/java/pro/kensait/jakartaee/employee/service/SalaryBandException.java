package pro.kensait.jakartaee.employee.service;

/*
 * 給与が業務上の範囲外であることを表す例外（実行時例外なので @Transactional はロールバックする）
 */
public class SalaryBandException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SalaryBandException(String message) {
        super(message);
    }
}
