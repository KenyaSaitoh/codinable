package pro.kensait.spring.employee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 * 役職情報を表すエンティティ
 */
@Entity
@Table(name = "JOB")
public class Job {
    @Id
    @Column(name = "JOB_ID")
    private Integer jobId;

    @Column(name = "JOB_NAME")
    private String jobName;

    @Column(name = "GRADE")
    private Integer grade;

    // アクセサメソッド
    public Integer getJobId() {
        return jobId;
    }

    // アクセサメソッド
    public String getJobName() {
        return jobName;
    }

    // 等級の取得
    public Integer getGrade() {
        return grade;
    }
}
