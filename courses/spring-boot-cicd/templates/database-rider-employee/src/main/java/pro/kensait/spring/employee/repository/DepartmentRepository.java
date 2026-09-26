package pro.kensait.spring.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pro.kensait.spring.employee.entity.Department;

/*
 * 部署情報を永続化するリポジトリ
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {
}
