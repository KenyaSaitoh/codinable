package pro.kensait.spring.employee.rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pro.kensait.spring.employee.rest.entity.Job;

/*
 * 役職マスターのリポジトリ
 */
public interface JobRepository extends JpaRepository<Job, Integer> {
}
