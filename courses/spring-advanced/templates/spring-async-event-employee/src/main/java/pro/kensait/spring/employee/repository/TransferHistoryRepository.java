package pro.kensait.spring.employee.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pro.kensait.spring.employee.entity.TransferHistory;

/*
 * 異動履歴のリポジトリ
 */
public interface TransferHistoryRepository extends JpaRepository<TransferHistory, Integer> {
    List<TransferHistory> findByEmployeeId(Integer employeeId);
}
