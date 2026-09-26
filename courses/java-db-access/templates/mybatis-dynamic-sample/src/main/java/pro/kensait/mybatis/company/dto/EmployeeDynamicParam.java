package pro.kensait.mybatis.company.dto;

import java.io.Serializable;

/*
 * 社員dynamicパラメータに使用するデータ
 */
public class EmployeeDynamicParam implements Serializable {

    private String departmentName;
    private Integer lowerSalary;
    private Integer upperSalary;

    // 社員dynamicパラメータの初期化
    public EmployeeDynamicParam() {
    }

    // 社員dynamicパラメータの初期化
    public EmployeeDynamicParam(String departmentName, Integer lowerSalary,
            Integer upperSalary) {
        this.departmentName = departmentName;
        this.lowerSalary = lowerSalary;
        this.upperSalary = upperSalary;
    }

    // 部署名へのアクセサメソッド
    public String getDepartmentName() {
        return departmentName;
    }

    // 部署名称の設定
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    // 下限月給の取得
    public Integer getLowerSalary() {
        return lowerSalary;
    }

    // 下限月給の設定
    public void setLowerSalary(Integer lowerSalary) {
        this.lowerSalary = lowerSalary;
    }

    // 上限月給の取得
    public Integer getUpperSalary() {
        return upperSalary;
    }

    // 上限月給の設定
    public void setUpperSalary(Integer upperSalary) {
        this.upperSalary = upperSalary;
    }
}
