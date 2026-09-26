package pro.kensait.mybatis.company.common;

import java.io.IOException;
import java.io.InputStream;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
/** Factoryは共有し、SqlSessionは処理ごとに生成して呼び出し元で閉じる */
public final class SqlSessionHolder {
    private static final SqlSessionHolder INSTANCE = new SqlSessionHolder();
    private final SqlSessionFactory factory;

    // SQLセッション保持の初期化
    private SqlSessionHolder() {
        try (InputStream input = Resources.getResourceAsStream("mybatis-config.xml")) {
            factory = new SqlSessionFactoryBuilder().build(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load mybatis-config.xml", exception);
        }
    }

    // instanceの取得
    public static SqlSessionHolder getInstance() {
        return INSTANCE;
    }

    // SQLセッションの取得
    public SqlSession getSqlSession() {
        return factory.openSession();
    }
}
