package pro.kensait.jpa.company.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 * projectの機能を提供するクラス
 */
@Entity
@Table(name = "PROJECT")
public class Project {
    // プロジェクトID
    @Id
    @Column(name = "PROJECT_ID")
    private Integer projectId;

    // プロジェクト名
    @Column(name = "PROJECT_NAME")
    private String projectName;

    // プラットフォーム
    @Column(name = "PLATFORM")
    private String platform;

    // 引数なしのコンストラクタ
    public Project() {
    }

    // コンストラクタ
    public Project(Integer projectId, String projectName, String platform) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.platform = platform;
    }

    // アクセサメソッド
    public Integer getProjectId() {
        return projectId;
    }

    // projectIDの設定
    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    // project名称の取得
    public String getProjectName() {
        return projectName;
    }

    // project名称の設定
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    // platformの取得
    public String getPlatform() {
        return platform;
    }

    // platformの設定
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Project [projectId=" + projectId + ", projectName=" + projectName + ", platform=" + platform + "]";
    }
}
