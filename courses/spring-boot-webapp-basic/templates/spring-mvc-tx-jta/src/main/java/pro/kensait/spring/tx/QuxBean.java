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
 * 2つめのXAデータソース（dataSource2）を更新するクラス
 * 引数が0未満の場合は例外をスローし、BarBeanが行った1つめのデータソースへの
 * 更新も含めて、グローバルトランザクション全体がロールバックされることを確認する
 */
@Service
@Transactional(TxType.REQUIRED)
public class QuxBean {
    private static final Logger logger = LoggerFactory.getLogger(
            QuxBean.class);

    // インジェクションポイント（2つめのXAデータソース）
    @Autowired
    @Qualifier("dataSource2")
    private DataSource ds;

    // 引数なしのコンストラクタ
    public QuxBean() {
    }

    // コンストラクタ
    public QuxBean(DataSource ds) {
        this.ds = ds;
    }

    // ビジネスメソッド
    public void doBusiness(int param) {
        logger.info("[ QuxBean#doBusiness ] Start");
        Connection conn = null;
        try {
            // BUSINESS_JTAテーブルの、主キーが"Qux"のローを更新する
            conn = ds.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE BUSINESS_JTA SET COUNT = COUNT + ? " +
                    "WHERE NAME = 'Qux'");
            pstmt.setInt(1, param);
            pstmt.executeUpdate();

            // 引数が0未満の場合は、例外をスローする
            if (param < 0) {
                throw new RuntimeException("param is invalid");
            }

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

        logger.info("[ QuxBean#doBusiness ] End");
    }
}