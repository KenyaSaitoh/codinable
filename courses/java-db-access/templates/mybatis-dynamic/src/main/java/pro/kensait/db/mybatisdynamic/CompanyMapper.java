package pro.kensait.db.mybatisdynamic;

import java.util.List;

/*
 * companyの契約を定義するインターフェース
 */
public interface CompanyMapper {
    // データの検索
    List<EmployeeView> search(EmployeeFilter filter);

    // summarize部署の実行
    List<DepartmentSummary> summarizeDepartments();
}
