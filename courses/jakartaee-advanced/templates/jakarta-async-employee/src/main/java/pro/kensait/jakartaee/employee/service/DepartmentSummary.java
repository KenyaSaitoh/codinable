package pro.kensait.jakartaee.employee.service;

import java.util.List;
import pro.kensait.jakartaee.employee.entity.Employee;

/*
 * 部署ごとの集計（部署ID・部署名・社員数・平均月給）
 */
public record DepartmentSummary(Integer departmentId, String departmentName,
        int employeeCount, int averageSalary) {

    public static DepartmentSummary of(Integer departmentId, List<Employee> members) {
        int average = (int) members.stream().mapToInt(Employee::getSalary).average().orElse(0);
        return new DepartmentSummary(departmentId, departmentName(departmentId), members.size(), average);
    }

    public static String departmentName(Integer departmentId) {
        if (departmentId == null) {
            return null;
        }
        return switch (departmentId) {
            case 1 -> "PLANNING";
            case 2 -> "HR";
            case 3 -> "SALES";
            case 4 -> "PRODUCT";
            default -> "UNKNOWN";
        };
    }
}
