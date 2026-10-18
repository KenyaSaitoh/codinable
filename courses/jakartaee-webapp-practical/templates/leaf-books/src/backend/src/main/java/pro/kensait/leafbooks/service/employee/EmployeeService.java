package pro.kensait.leafbooks.service.employee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import pro.kensait.leafbooks.dao.EmployeeDao;
import pro.kensait.leafbooks.entity.Employee;

/**
 * 社員（書店の管理者）の照会を行うサービスクラス
 */
@ApplicationScoped
public class EmployeeService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @Inject
    private EmployeeDao employeeDao;

    /**
     * 社員コードで社員を取得する（見つからなければ null）
     */
    public Employee getEmployeeByCode(String employeeCode) {
        logger.info("[ EmployeeService#getEmployeeByCode ] employeeCode: {}", employeeCode);
        return employeeDao.findByCode(employeeCode);
    }
}
