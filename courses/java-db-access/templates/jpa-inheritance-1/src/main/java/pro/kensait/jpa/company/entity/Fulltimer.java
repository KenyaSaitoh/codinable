package pro.kensait.jpa.company.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/*
 * 正社員の機能を提供するクラス
 */
@Entity
@DiscriminatorValue(value = "1")
public class Fulltimer extends Employee {
    // 役職名
    @Column(name = "JOB_NAME")
    private String jobName;

    // 月給（社員）
    @Column(name = "SALARY")
    private Integer salary;

    // 引数なしのコンストラクタ
    public Fulltimer() {
        super();
    }

    // コンストラクタ
    public Fulltimer(Integer employeeId, String employeeName, Department department,
            Integer employeeType, LocalDate entranceDate, String jobName,
            Integer salary) {
        super(employeeId, employeeName, department, employeeType, entranceDate);
        this.jobName = jobName;
        this.salary = salary;
    }

    // アクセサメソッド
    public String getJobName() {
        return jobName;
    }

    // 役職名称の設定
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    // getsalaryの実行
    public Integer getsalary() {
        return salary;
    }

    // setsalaryの実行
    public void setsalary(Integer salary) {
        this.salary = salary;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Fulltimer [jobName=" + jobName + ", salary=" + salary + ", employeeId="
                + employeeId + ", employeeName=" + employeeName + ", department="
                + department + ", entranceDate=" + entranceDate + "]";
    }
}
