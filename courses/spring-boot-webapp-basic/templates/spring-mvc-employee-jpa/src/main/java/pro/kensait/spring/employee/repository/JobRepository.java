package pro.kensait.spring.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pro.kensait.spring.employee.entity.Job;

/*
 * 役職マスターのリポジトリ
 */
public interface JobRepository extends JpaRepository<Job, Integer> {
}
