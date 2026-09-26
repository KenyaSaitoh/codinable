package pro.kensait.mybatis.company.dto;

import java.io.Serializable;

/*
 * 社員月給パラメータに使用するデータ
 */
public class EmployeeSalaryParam implements Serializable {
    // 月給
    private int salary;
    // 削減額
    private int payCut;

    // 社員月給パラメータの初期化
    public EmployeeSalaryParam(int salary, int payCut) {
        this.salary = salary;
        this.payCut = payCut;
    }

    // getsalaryの実行
    public int getsalary() {
        return salary;
    }

    // setsalaryの実行
    public void setsalary(int salary) {
        this.salary = salary;
    }

    // paycutの取得
    public int getPayCut() {
        return payCut;
    }

    // paycutの設定
    public void setPayCut(int payCut) {
        this.payCut = payCut;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "EmployeeSalaryParam [salary=" + salary + ", payCut=" + payCut + "]";
    }
}
