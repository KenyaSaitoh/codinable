package pro.kensait.db.mybatis;

/*
 * 社員の契約を定義するインターフェース
 */
public interface EmployeeMapper {
    // 社員の登録
    int insert(Employee employee);

    // リポジトリメソッド：主キー検索によって社員の取得
    Employee findById(int id);

    // 一件更新
    int update(Employee employee);

    // APIメソッド：Employeeの削除
    int delete(int id);
}
