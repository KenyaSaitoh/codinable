package pro.kensait.spring.employee.rest.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/*
 * データベースへの疎通を確認するカスタムヘルスインジケーターを表すクラス
 * （/actuator/healthのレスポンスに"databaseHealthIndicator"の名前から
 * 　導出された"database"コンポーネントとして表示される）
 */
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    private static final Logger logger = LoggerFactory.getLogger(
            DatabaseHealthIndicator.class);

    // インジェクションポイント
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // コンストラクタ
    public DatabaseHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ヘルスチェックメソッド：EMPLOYEEテーブルの件数取得によりDBの稼働状態を判定する
    // ECSデプロイ時はALBターゲットグループのヘルスチェックが/actuator/healthを監視し、
    // DOWN応答（ステータス503）が続くとタスクは異常と見なされ、自動的に再起動
    @Override
    public Health health() {
        logger.info("[ DatabaseHealthIndicator#health ]");
        try {
            // EMPLOYEEテーブルの件数を取得する（取得できればDBは正常と判定する）
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM EMPLOYEE", Integer.class);

            // ステータスUPに件数を付加したHealthを生成し、返す
            return Health.up()
                    .withDetail("database", "HSQLDB")
                    .withDetail("employeeCount", count)
                    .build();

        } catch (Exception ex) {
            // 例外が発生した場合（DB停止など）はステータスDOWNのHealthを生成し、返す
            return Health.down(ex).build();
        }
    }
}
