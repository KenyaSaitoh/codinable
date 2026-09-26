package pro.kensait.jpa.company.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/*
 * 社員を表すエンティティクラス
 */
@Entity
@Table(name = "EMPLOYEE")
public class Employee {
    // 社員ID
    @Id
    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    // 社員名
    @Column(name = "EMPLOYEE_NAME")
    private String employeeName;

    // 住所（1対1、単方向）
    @OneToOne(targetEntity = Address.class)
    @JoinColumn(name = "ADDRESS_ID",
            referencedColumnName = "ADDRESS_ID")
    private Address address;

    // 電話（1対1、双方向）
    @OneToOne(targetEntity = Phone.class,
            mappedBy = "employee")
    private Phone phone;

    // メールリスト（1対多、単方向）
    @OneToMany(targetEntity = Email.class)
    @JoinColumn(name = "HOLDER_ID",
            referencedColumnName = "EMPLOYEE_ID")
    private List<Email> emails;

    // 部署（多対1、双方向）
    @ManyToOne(targetEntity = Department.class)
    @JoinColumn(name = "DEPARTMENT_ID",
            referencedColumnName = "DEPARTMENT_ID")
    private Department department;

    // 入社年月日
    @Column(name = "ENTRANCE_DATE")
    private LocalDate entranceDate;

    // 役職（多対1、単方向）
    @ManyToOne(targetEntity = Job.class)
    @JoinColumn(name = "JOB_ID",
            referencedColumnName = "JOB_ID")
    private Job job;

    // 月給
    @Column(name = "SALARY")
    private Integer salary;

    // 担当プロジェクトリスト（多対多、単方向）
    @ManyToMany(targetEntity = Project.class)
    @JoinTable(name = "EMPLOYEE_PROJECT",
            joinColumns=@JoinColumn(
                    name = "EMPLOYEE_ID",
                    referencedColumnName = "EMPLOYEE_ID"),
            inverseJoinColumns=@JoinColumn(
                    name = "PROJECT_ID",
                    referencedColumnName = "PROJECT_ID"))
    private List<Project> projects;

    // 保有資格リスト（多対多、双方向）
    @ManyToMany(targetEntity = Qualification.class)
    @JoinTable(name = "EMPLOYEE_QUALIFICATION",
            joinColumns=@JoinColumn(
                    name = "EMPLOYEE_ID",
                    referencedColumnName = "EMPLOYEE_ID"),
            inverseJoinColumns=@JoinColumn(
                    name = "QUALIFICATION_ID",
                    referencedColumnName = "QUALIFICATION_ID"))
    private List<Qualification> qualifications;

    // 引数なしのコンストラクタ
    public Employee() {
    }

    // コンストラクタ
    public Employee(Integer employeeId, String employeeName, Address address,
            Phone phone, List<Email> emails, Department department,
            LocalDate entranceDate, Job job, Integer salary,
            List<Project> projects, List<Qualification> qualifications) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.address = address;
        this.phone = phone;
        this.emails = emails;
        this.department = department;
        this.entranceDate = entranceDate;
        this.job = job;
        this.salary = salary;
        this.projects = projects;
        this.qualifications = qualifications;
    }

    /* コンストラクタ
     * 循環参照にならないように、Employeeが直接保持する属性（関連を持たない属性）と、
     * 関連はあっても単方向（かつテーブル上も外部キー参照されていない）関連のみのコンストラクタを作る
     * Emailは単方向だが、テーブル上で外部キー参照されているので、コンストラクタには定義せず、
     * Employeeの後にINSERTされるようにする必要がある
     */
    // コンストラクタ
    public Employee(Integer employeeId, String employeeName, Address address,
            LocalDate entranceDate, Job job, Integer salary,  List<Project> projects) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.address = address;
        this.entranceDate = entranceDate;
        this.job = job;
        this.salary = salary;
        this.projects = projects;
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

    // 住所の取得
    public Address getAddress() {
        return address;
    }

    // 住所の設定
    public void setAddress(Address address) {
        this.address = address;
    }

    // 電話番号の取得
    public Phone getPhone() {
        return phone;
    }

    // 電話番号の設定
    public void setPhone(Phone phone) {
        this.phone = phone;
    }

    // emailsの取得
    public List<Email> getEmails() {
        return emails;
    }

    // emailsの設定
    public void setEmails(List<Email> emails) {
        this.emails = emails;
    }


    // 部署へのアクセサメソッド
    public Department getDepartment() {
        return department;
    }

    // 部署の設定
    public void setDepartment(Department department) {
        this.department = department;
    }

    // 入社年月日へのアクセサメソッド
    public LocalDate getEntranceDate() {
        return entranceDate;
    }

    // 入社日付の設定
    public void setEntranceDate(LocalDate entranceDate) {
        this.entranceDate = entranceDate;
    }

    // 役職の取得
    public Job getJob() {
        return job;
    }

    // 役職の設定
    public void setJob(Job job) {
        this.job = job;
    }

    // 月給へのアクセサメソッド
    public Integer getSalary() {
        return salary;
    }

    // 月給の設定
    public void setSalary(Integer salary) {
        this.salary = salary;
    }


    // 射影の取得
    public List<Project> getProjects() {
        return projects;
    }

    // 射影の設定
    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    // qualificationsの取得
    public List<Qualification> getQualifications() {
        return qualifications;
    }

    // qualificationsの設定
    public void setQualifications(List<Qualification> qualifications) {
        this.qualifications = qualifications;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Employee [employeeId=" + employeeId + ", employeeName=" + employeeName + ", address=" + address
                + ", phone=" + phone + ", emails=" + emails + ", department=" + department + ", entranceDate="
                + entranceDate + ", job=" + job + ", salary=" + salary + ", projects=" + projects + ", qualifications="
                + qualifications + "]";
    }
}
