package pro.kensait.spring.employee.mapper;

import java.util.List;

import pro.kensait.spring.employee.entity.Job;

/*
 * 役職の契約を定義するインターフェース
 */
public interface JobMapper {
    // 全件検索
    List<Job> findAll();

    // リポジトリメソッド：主キー検索によって社員の取得
    Job findById(Integer jobId);
}
