package pro.kensait.spring.employee.web;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import pro.kensait.spring.employee.entity.Employee;
/** 社員入力フォーム */
public class EmployeeParam {
    private static final int NAME_MAX = 30;
    private static final int SALARY_MAX = 9_999_999;

    private Integer employeeId;
    private String employeeName = "";
    private Integer departmentId;
    private Integer jobId;
    private String salary = "";
    private String entranceDate = "";
    private Integer version;

    // 社員パラメータの検証
    public List<String> validate(boolean departmentExists, boolean jobExists) {
        List<String> errors = new ArrayList<>();
        String name = employeeName == null ? "" : employeeName.strip();
        if (name.isEmpty()) {
            errors.add("氏名を入力してください");
        } else if (name.codePointCount(0, name.length()) > NAME_MAX) {
            errors.add("氏名は30文字以内で入力してください");
        }
        if (!departmentExists) {
            errors.add("部署を選んでください");
        }
        if (!jobExists) {
            errors.add("役職を選んでください");
        }
        if (salaryValue() == null) {
            errors.add("月給は0以上9999999以下で入力してください");
        }
        if (entranceDateValue() == null) {
            errors.add("入社日を入力してください");
        }
        return errors;
    }

    // 月給値の実行
    public Integer salaryValue() {
        try {
            int value = Integer.parseInt(salary == null ? "" : salary.strip());
            return 0 <= value && value <= SALARY_MAX ? value : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    // 入社日付値の実行
    public LocalDate entranceDateValue() {
        try {
            return LocalDate.parse(entranceDate == null ? "" : entranceDate.strip());
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    // リクエストを社員エンティティへの変換
    public Employee toEmployee() {
        Employee employee = new Employee();
        employee.setEmployeeName(employeeName == null ? null : employeeName.strip());
        employee.setDepartmentId(departmentId);
        employee.setJobId(jobId);
        employee.setSalary(salaryValue());
        employee.setEntranceDate(entranceDateValue());
        return employee;
    }

    // 社員パラメータの生成
    public static EmployeeParam of(Employee employee) {
        EmployeeParam param = new EmployeeParam();
        param.employeeId = employee.getEmployeeId();
        param.employeeName = employee.getEmployeeName();
        param.departmentId = employee.getDepartmentId();
        param.jobId = employee.getJobId();
        param.salary = String.valueOf(employee.getSalary());
        param.entranceDate = String.valueOf(employee.getEntranceDate());
        param.version = employee.getVersion();
        return param;
    }

    // アクセサメソッド
    public Integer getEmployeeId() {
        return employeeId;
    }

    // 社員IDの設定
    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    // 社員名へのアクセサメソッド
    public String getEmployeeName() {
        return employeeName;
    }

    // 社員名称の設定
    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    // アクセサメソッド
    public Integer getDepartmentId() {
        return departmentId;
    }

    // 部署IDの設定
    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    // アクセサメソッド
    public Integer getJobId() {
        return jobId;
    }

    // 役職IDの設定
    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    // 月給へのアクセサメソッド
    public String getSalary() {
        return salary;
    }

    // 月給の設定
    public void setSalary(String salary) {
        this.salary = salary;
    }

    // 入社年月日へのアクセサメソッド
    public String getEntranceDate() {
        return entranceDate;
    }

    // 入社日付の設定
    public void setEntranceDate(String entranceDate) {
        this.entranceDate = entranceDate;
    }

    // バージョン（楽観的ロックで使用）へのアクセサメソッド
    public Integer getVersion() {
        return version;
    }

    // バージョンの設定
    public void setVersion(Integer version) {
        this.version = version;
    }
}
