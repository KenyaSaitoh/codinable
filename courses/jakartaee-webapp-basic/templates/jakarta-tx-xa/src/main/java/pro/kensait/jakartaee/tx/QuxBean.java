package pro.kensait.jakartaee.tx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

@ApplicationScoped
@Transactional(TxType.REQUIRED)
public class QuxBean {
    private static final Logger logger =
            Logger.getLogger(QuxBean.class.getName());

    @Resource(lookup = "java:app/jdbc/TxDS2")
    private DataSource ds;

    public void doBusiness(int param) {
        logger.info("[ QuxBean#doBusiness ] Start");
        try (Connection conn = ds.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "UPDATE BUSINESS_XA SET COUNT = COUNT + ? "
                        + "WHERE NAME = 'Qux'")) {
            pstmt.setInt(1, param);
            pstmt.executeUpdate();
            if (param < 0) {
                logger.info("[ QuxBean#doBusiness ] param is invalid");
                throw new RuntimeException("param is invalid");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        logger.info("[ QuxBean#doBusiness ] End");
    }
}
