package pro.kensait.spring.tx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

/*
 * 1つめのXAデータソース（dataSource1）を更新するクラス
 * ローカルトランザクション版（spring-mvc-tx）ではREQUIRES_NEWとの対比を行ったが、
 * ここでは2つのデータソースを同一のグローバルトランザクションに参加させるため、
 * REQUIREDを指定して呼び出し元（FooBean）のトランザクションに参加する
 */
@Service
@Transactional(TxType.REQUIRED)
public class BarBean {
    private static final Logger logger = LoggerFactory.getLogger(
            BarBean.class);

    // インジェクションポイント（1つめのXAデータソース）
    @Autowired
    @Qualifier("dataSource1")
    private DataSource ds;

    // 引数なしのコンストラクタ
    public BarBean() {
    }

    // コンストラクタ
    public BarBean(DataSource ds) {
        this.ds = ds;
    }

    // ビジネスメソッド
    public void doBusiness(int param) {
        logger.info("[ BarBean#doBusiness ] Start");
        Connection conn = null;
        try {
            // BUSINESS_JTAテーブルの、主キーが"Bar"のローを更新する
            // XAデータソースから取得したコネクションは、
            // JTAのグローバルトランザクションに自動的に参加する
            conn = ds.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE BUSINESS_JTA SET COUNT = COUNT + ? " +
                    "WHERE NAME = 'Bar'");
            pstmt.setInt(1, param);
            pstmt.executeUpdate();

        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        } finally {
            // コネクションをクローズする
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                throw new RuntimeException(sqle);
            }
        }

        logger.info("[ BarBean#doBusiness ] End");
    }
}