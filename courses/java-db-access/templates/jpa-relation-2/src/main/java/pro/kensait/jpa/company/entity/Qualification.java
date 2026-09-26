package pro.kensait.jpa.company.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/*
 * 資格の機能を提供するクラス
 */
@Entity
@Table(name = "QUALIFICATION")
public class Qualification {
    // 資格ID
    @Id
    @Column(name = "QUALIFICATION_ID")
    private Integer qualificationId;

    // 資格名
    @Column(name = "QUALIFICATION_NAME")  
    private String qualificationName;

    // 資格種別
    @Column(name = "QUALIFICATION_TYPE")
    private String qualificationType;

    // 社員リスト
    @ManyToMany(targetEntity = Employee.class,
            mappedBy = "qualifications")
    private List<Employee> employees;

    // 引数なしのコンストラクタ
    public Qualification() {
    }

    // コンストラクタ
    public Qualification(Integer qualificationId, String qualificationName,
            String qualificationType, List<Employee> employees) {
        this.qualificationId = qualificationId;
        this.qualificationName = qualificationName;
        this.qualificationType = qualificationType;
        this.employees = employees;
    }

    // アクセサメソッド
    public Integer getQualificationId() {
        return qualificationId;
    }

    // 資格IDの設定
    public void setQualificationId(Integer qualificationId) {
        this.qualificationId = qualificationId;
    }

    // 資格名称の取得
    public String getQualificationName() {
        return qualificationName;
    }

    // 資格名称の設定
    public void setQualificationName(String qualificationName) {
        this.qualificationName = qualificationName;
    }

    // 資格型の取得
    public String getQualificationType() {
        return qualificationType;
    }

    // 資格型の設定
    public void setQualificationType(String qualificationType) {
        this.qualificationType = qualificationType;
    }

    // 各しきい値はapplication.ymlのresilience4j.circuitbreaker.instances.employeeApiで設定
    public List<Employee> getEmployees() {
        return employees;
    }

    // 社員の設定
    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Qualification [qualificationId=" + qualificationId + ", qualificationName=" + qualificationName
                + ", qualificationType=" + qualificationType + "]";
    }
}
