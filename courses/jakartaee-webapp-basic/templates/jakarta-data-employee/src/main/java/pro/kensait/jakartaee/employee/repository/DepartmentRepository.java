package pro.kensait.jakartaee.employee.repository;

import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.Repository;
import pro.kensait.jakartaee.employee.entity.Department;

/** 読み取りだけなので BasicRepository を継承する（実装クラスは Jakarta Data のプロバイダーが生成する） */
@Repository
public interface DepartmentRepository extends BasicRepository<Department, Integer> {
}
