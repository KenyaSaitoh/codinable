package pro.kensait.leafbooks.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pro.kensait.leafbooks.entity.Category;

/*
 * カテゴリ情報の永続化を担うリポジトリ
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
