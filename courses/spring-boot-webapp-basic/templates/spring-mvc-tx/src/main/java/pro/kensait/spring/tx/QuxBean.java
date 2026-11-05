package pro.kensait.spring.tx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/*
 * BUSINESSテーブルの"Qux"のローを更新するクラス
 * REQUIREDを指定し、呼び出し元（FooBean）のトランザクションに参加する
 * 引数が0未満の場合は更新後に例外をスローし、この更新を取り消す
 */
@Service
@Transactional(propagation = Propagation.REQUIRED)
public class QuxBean {
    private static final Logger logger = LoggerFactory.getLogger(
            QuxBean.class);

    // JdbcTemplateは、現在のトランザクションの接続でSQLを実行する
    private final JdbcTemplate jdbcTemplate;

    // コンストラクタ
    public QuxBean(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ビジネスメソッド
    public void doBusiness(int param) {
        logger.info("[ QuxBean#doBusiness ] Start");

        // BUSINESSテーブルの、主キーが"Qux"のローを更新する
        jdbcTemplate.update(
                "UPDATE BUSINESS SET COUNT = COUNT + ? WHERE NAME = 'Qux'",
                param);

        // 引数が0未満の場合は、例外をスローする
        if (param < 0) {
            throw new RuntimeException("param is invalid");
        }

        logger.info("[ QuxBean#doBusiness ] End");
    }
}
