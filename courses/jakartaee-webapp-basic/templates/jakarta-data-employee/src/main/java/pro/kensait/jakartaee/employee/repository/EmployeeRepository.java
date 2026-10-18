package pro.kensait.jakartaee.employee.repository;

import java.util.List;
import java.util.Optional;

import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import pro.kensait.jakartaee.employee.entity.Employee;

/**
 * 社員の Repository。インタフェースの宣言だけを書き、実装クラスは Jakarta Data のプロバイダーが生成して
 * CDI Bean として登録する（Payara Server ではアプリケーションの永続化ユニット EmployeePU を使う）
 */
@Repository
public interface EmployeeRepository
        extends CrudRepository<Employee, Integer> {

    // メソッド名による問い合わせ（Query by Method Name）
    long countByStatus(String status);

    List<Employee> findByStatusAndJobJobIdOrderByEmployeeId(
            String status, Integer jobId);

    boolean existsByEmployeeCode(String employeeCode);

    // @Find: 引数を属性に対応させた問い合わせ
    @Find
    Optional<Employee> byCode(@By("employeeCode") String code);

    @Find
    @OrderBy("employeeId")
    Page<Employee> byStatus(String status, PageRequest pageRequest);

    // @Query: JDQL の問い合わせ。select は戻り値から決まるので省略している
    // （演習用: JDQL では from も省略できるが、Payara 7.2026.9 は Page を返す問い合わせで from の無い JDQL を扱えないため書いている。
    //   並び順も、Payara 7.2026.9 では @OrderBy が @Query の結果に効かなかったため、問い合わせの order by で指定している）
    @Query("""
            from Employee
            where status = 'active'
              and employeeName like :pattern
              and salary between :salaryFrom and :salaryTo
            order by employeeId
            """)
    Page<Employee> searchByNameAndSalary(
            @Param("pattern") String pattern,
            @Param("salaryFrom") int salaryFrom,
            @Param("salaryTo") int salaryTo,
            PageRequest pageRequest);
}
