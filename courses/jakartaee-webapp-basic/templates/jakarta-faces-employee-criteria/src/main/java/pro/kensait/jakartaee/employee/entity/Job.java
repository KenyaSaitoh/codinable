package pro.kensait.jakartaee.employee.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 役職マスター（JOB テーブル）。主キーは 1〜4 の固定値なので @GeneratedValue は付けない */
@Entity
@Table(name = "JOB")
public class Job implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "JOB_ID")
    private Integer jobId;

    @Column(name = "JOB_NAME")
    private String jobName;

    @Column(name = "GRADE")
    private Integer grade;

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }
}
