package pro.kensait.spring.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import pro.kensait.spring.employee.entity.Job;
/** 第10章の職種マスターHTTPでの書き込みはRestConfigで禁止する */
@RepositoryRestResource(path = "jobs", collectionResourceRel = "jobs")
public interface JobRepository extends JpaRepository<Job, Integer> {
}
