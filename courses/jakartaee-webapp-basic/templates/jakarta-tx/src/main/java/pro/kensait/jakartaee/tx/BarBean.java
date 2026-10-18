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
@Transactional(TxType.REQUIRES_NEW)
public class BarBean {
    private static final Logger logger =
            Logger.getLogger(BarBean.class.getName());

    @Resource(lookup = "java:app/jdbc/TxDS")
    private DataSource ds;

    public void doBusiness(int param) {
        logger.info("[ BarBean#doBusiness ] Start");
        try (Connection conn = ds.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "UPDATE BUSINESS SET COUNT = COUNT + ? "
                        + "WHERE NAME = 'Bar'")) {
            pstmt.setInt(1, param);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        logger.info("[ BarBean#doBusiness ] End");
    }
}
