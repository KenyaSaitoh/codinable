package pro.kensait.jpa.company.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/* 役職を表すエンティティクラス */
@Entity
@Table(name = "JOB")
public class Job {
    // 役職ID
    @Id
    @Column(name = "JOB_ID")
    private Integer jobId;

    // 役職名
    @Column(name = "JOB_NAME")
    private String jobName;

    // 引数なしのコンストラクタ
    public Job() {
    }

    // コンストラクタ
    public Job(Integer jobId, String jobName) {
        this.jobId = jobId;
        this.jobName = jobName;
    }

    // アクセサメソッド
    public Integer getJobId() {
        return jobId;
    }

    // 役職IDの設定
    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    // アクセサメソッド
    public String getJobName() {
        return jobName;
    }

    // 役職名称の設定
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Job [jobId=" + jobId + ", jobName=" + jobName + "]";
    }
}
