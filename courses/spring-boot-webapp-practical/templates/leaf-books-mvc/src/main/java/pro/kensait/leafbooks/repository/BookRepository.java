package pro.kensait.leafbooks.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pro.kensait.leafbooks.entity.Book;

/*
 * 書籍情報の永続化を担うリポジトリ
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    // 書籍の検索
    @Query("SELECT b FROM Book b WHERE b.category.categoryId = :categoryId")
    List<Book> query(@Param("categoryId") Integer categoryId);

    // 書籍の検索
    @Query("SELECT b FROM Book b WHERE b.bookName like :keyword")
    List<Book> query(@Param("keyword") String keyword);

    // 書籍の検索
    @Query("SELECT b FROM Book b WHERE b.category.categoryId = :categoryId "
            + "AND b.bookName like :keyword")
    List<Book> query(@Param("categoryId") Integer categoryId,
            @Param("keyword") String keyword);
}
