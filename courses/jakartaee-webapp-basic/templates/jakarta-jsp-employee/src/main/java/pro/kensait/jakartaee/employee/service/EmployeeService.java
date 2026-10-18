package pro.kensait.jakartaee.employee.service;

import java.util.List;

/**
 * 社員の検索と削除の手順をまとめる業務処理のクラス（Model）
 * データの保持は EmployeeDAO に任せる。サーブレットは EmployeeDAO を直接知らない
 */
public class EmployeeService {
    private final EmployeeDAO employeeDao;

    public EmployeeService(EmployeeDAO employeeDao) {
        this.employeeDao = employeeDao;
    }

    public List<Employee> findAll() {
        return employeeDao.findAll();
    }

    public int delete(int employeeId) {
        return employeeDao.delete(employeeId);
    }
}
