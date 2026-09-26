package pro.kensait.leafbooks.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import pro.kensait.leafbooks.entity.Book;

/*
 * 書籍のテスト
 */
@DataJpaTest
@Sql(scripts = "/test-data.sql")
@DisplayName("BookRepositoryのテスト")
class BookRepositoryTest {
    
    @Autowired
    private BookRepository bookRepository;
    
    // 「IDで書籍を検索できること」の検証
    @Test
    @DisplayName("IDで書籍を検索できること")
    void test_findById() {
        // 実行フェーズ：IDで書籍を検索する
        Optional<Book> found = bookRepository.findById(1);
        
        // 検証フェーズ：期待値と実測値が一致しているかを検証する
        assertTrue(found.isPresent());
        assertEquals("Java SEディープダイブ", found.get().getBookName());
        assertEquals("Michael Johnson", found.get().getAuthor());
    }
    
    // 「カテゴリIDで書籍を検索できること」の検証
    @Test
    @DisplayName("カテゴリIDで書籍を検索できること")
    void test_queryByCategoryId() {
        // 実行フェーズ：カテゴリIDで書籍を検索する
        List<Book> books = bookRepository.query(1);
        
        // 検証フェーズ：期待値と実測値が一致しているかを検証する
        assertNotNull(books);
        assertEquals(3, books.size()); // カテゴリID=1(Java)の書籍は3件
        assertEquals(1, books.get(0).getCategory().getCategoryId());
    }
    
    // 「キーワードで書籍を検索できること」の検証
    @Test
    @DisplayName("キーワードで書籍を検索できること")
    void test_queryByKeyword() {
        // 実行フェーズ：キーワードで書籍を検索する
        List<Book> books = bookRepository.query("%Spring%");
        
        // 検証フェーズ：期待値と実測値が一致しているかを検証する
        assertNotNull(books);
        assertEquals(3, books.size()); // "Spring"を含む書籍は3件
        assertTrue(books.get(0).getBookName().contains("Spring"));
    }
    
    // 「カテゴリIDとキーワードで書籍を検索できること」の検証
    @Test
    @DisplayName("カテゴリIDとキーワードで書籍を検索できること")
    void test_queryByCategoryIdAndKeyword() {
        // 実行フェーズ：カテゴリIDとキーワードで書籍を検索する
        List<Book> books = bookRepository.query(2, "%API%");
        
        // 検証フェーズ：期待値と実測値が一致しているかを検証する
        assertNotNull(books);
        assertTrue(books.size() >= 1);
        assertEquals(2, books.get(0).getCategory().getCategoryId());
        assertTrue(books.get(0).getBookName().contains("API"));
    }
}
