package pro.kensait.spring.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import pro.kensait.spring.employee.entity.Job;
/** 役職マスター。HTTPでの書き込みはRestConfigで禁止する */
@RepositoryRestResource(path = "jobs", collectionResourceRel = "jobs")
public interface JobRepository extends JpaRepository<Job, Integer> {
}
