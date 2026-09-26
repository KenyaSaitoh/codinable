package pro.kensait.spring.tx;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.atomikos.jdbc.AtomikosDataSourceBean;

/*
 * JTA（Atomikos）が管理するXAデータソースを構成するJava Configクラス
 * ローカルトランザクションと異なり、JTAでは2フェーズコミットに対応した
 * XAデータソースを、トランザクションマネージャに「リソース」として登録する
 */
@Configuration
public class XaDataSourceConfig {

    // 1つめのデータソースの接続情報（application.ymlから注入される）
    @Value("${app.datasource1.url}")
    private String url1;
    @Value("${app.datasource1.username}")
    private String username1;
    @Value("${app.datasource1.password}")
    private String password1;

    // 2つめのデータソースの接続情報（application.ymlから注入される）
    @Value("${app.datasource2.url}")
    private String url2;
    @Value("${app.datasource2.username}")
    private String username2;
    @Value("${app.datasource2.password}")
    private String password2;

    // 1つめのXAデータソース（BarBeanが利用する）
    @Primary
    @Bean(initMethod = "init", destroyMethod = "close")
    public AtomikosDataSourceBean dataSource1() {
        // HSQLDBのXAデータソース（JDBCXADataSource）をAtomikosに登録する
        AtomikosDataSourceBean ds = new AtomikosDataSourceBean();
        ds.setUniqueResourceName("xaDataSource1");
        ds.setXaDataSourceClassName("org.hsqldb.jdbc.pool.JDBCXADataSource");
        Properties props = new Properties();
        props.setProperty("url", url1);
        props.setProperty("user", username1);
        props.setProperty("password", password1);
        ds.setXaProperties(props);
        ds.setPoolSize(5);
        return ds;
    }

    // 2つめのXAデータソース（QuxBeanが利用する）
    @Bean(initMethod = "init", destroyMethod = "close")
    public AtomikosDataSourceBean dataSource2() {
        // HSQLDBのXAデータソース（JDBCXADataSource）をAtomikosに登録する
        AtomikosDataSourceBean ds = new AtomikosDataSourceBean();
        ds.setUniqueResourceName("xaDataSource2");
        ds.setXaDataSourceClassName("org.hsqldb.jdbc.pool.JDBCXADataSource");
        Properties props = new Properties();
        props.setProperty("url", url2);
        props.setProperty("user", username2);
        props.setProperty("password", password2);
        ds.setXaProperties(props);
        ds.setPoolSize(5);
        return ds;
    }
}