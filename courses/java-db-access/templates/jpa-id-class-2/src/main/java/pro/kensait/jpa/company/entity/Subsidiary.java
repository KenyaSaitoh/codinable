package pro.kensait.jpa.company.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/*
 * subsidiaryの機能を提供するクラス
 */
@Entity
@Table(name = "SUBSIDIARY")
public class Subsidiary {
    // 分社ID
    @Id
    @Column(name = "SUBSIDIARY_ID")
    private Integer subsidiaryId;

    // 分社名
    @Column(name = "SUBSIDIARY_NAME")
    private String subsidiaryName;

    // 所在地
    @Column(name = "LOCATION")
    private String location;

    // 社員リスト
    @OneToMany(targetEntity = Employee.class,
            mappedBy = "subsidiary",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<Employee> employees = new ArrayList<Employee>();

    // 引数なしのコンストラクタ
    public Subsidiary() {
    }

    // コンストラクタ
    public Subsidiary(Integer subsidiaryId, String subsidiaryName, String location,
            List<Employee> employees) {
        this.subsidiaryId = subsidiaryId;
        this.subsidiaryName = subsidiaryName;
        this.location = location;
        this.employees = employees;
    }

    // アクセサメソッド
    public Integer getSubsidiaryId() {
        return subsidiaryId;
    }

    // subsidiaryIDの設定
    public void setSubsidiaryId(Integer subsidiaryId) {
        this.subsidiaryId = subsidiaryId;
    }

    // アクセサメソッド
    public String getSubsidiaryName() {
        return subsidiaryName;
    }

    // subsidiary名称の設定
    public void setSubsidiaryName(String subsidiaryName) {
        this.subsidiaryName = subsidiaryName;
    }

    // 所在地へのアクセサメソッド
    public String getLocation() {
        return location;
    }

    // 所在地の設定
    public void setLocation(String location) {
        this.location = location;
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
        return "Subsidiary [subsidiaryId=" + subsidiaryId + ", subsidiaryName=" + subsidiaryName
                + ", location=" + location + ", employees=" + employees + "]";
    }
}
