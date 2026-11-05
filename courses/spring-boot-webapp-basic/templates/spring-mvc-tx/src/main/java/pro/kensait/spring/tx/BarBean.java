package pro.kensait.spring.tx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/*
 * BUSINESSテーブルの"Bar"のローを更新するクラス
 * REQUIRES_NEWを指定し、呼び出し元（FooBean）のトランザクションを中断して、
 * 独立したトランザクションで更新を確定する
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class BarBean {
    private static final Logger logger = LoggerFactory.getLogger(
            BarBean.class);

    // JdbcTemplateは、現在のトランザクションの接続でSQLを実行する
    private final JdbcTemplate jdbcTemplate;

    // コンストラクタ
    public BarBean(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ビジネスメソッド
    public void doBusiness(int param) {
        logger.info("[ BarBean#doBusiness ] Start");

        // BUSINESSテーブルの、主キーが"Bar"のローを更新する
        jdbcTemplate.update(
                "UPDATE BUSINESS SET COUNT = COUNT + ? WHERE NAME = 'Bar'",
                param);

        logger.info("[ BarBean#doBusiness ] End");
    }
}
