package pro.kensait.db.mybatisdynamic;

import java.math.BigDecimal;
import java.util.List;

/*
 * 社員の機能を提供するクラス
 */
public final class EmployeeFilter {
    private final Integer departmentId;
    private final BigDecimal minimumSalary;
    private final List<Integer> ids;

    // 社員の初期化
    public EmployeeFilter(Integer departmentId, BigDecimal minimumSalary, List<Integer> ids) {
        this.departmentId = departmentId;
        this.minimumSalary = minimumSalary;
        this.ids = List.copyOf(ids);
    }

    // アクセサメソッド
    public Integer getDepartmentId() {
        return departmentId;
    }

    // minimum月給の取得
    public BigDecimal getMinimumSalary() {
        return minimumSalary;
    }

    // ID一覧の取得
    public List<Integer> getIds() {
        return List.copyOf(ids);
    }
}
