package pro.kensait.jakartaee.employee.repository;

import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.Repository;
import pro.kensait.jakartaee.employee.entity.Job;

/** 読み取りだけなので BasicRepository を継承する（実装クラスは Jakarta Data のプロバイダーが生成する） */
@Repository
public interface JobRepository extends BasicRepository<Job, Integer> {
}
