package pro.kensait.spring.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import pro.kensait.spring.employee.entity.Department;
/** 第10章の部署マスターHTTPでの書き込みはRestConfigで禁止する */
@RepositoryRestResource(path = "departments", collectionResourceRel = "departments")
public interface DepartmentRepository extends JpaRepository<Department, Integer> {
}
