package pro.kensait.spring.employee.rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pro.kensait.spring.employee.rest.entity.Job;

/*
 * 第10章の職種マスターHTTPでの書き込みはRestConfigで禁止する
 */
public interface JobRepository extends JpaRepository<Job, Integer> {
}
