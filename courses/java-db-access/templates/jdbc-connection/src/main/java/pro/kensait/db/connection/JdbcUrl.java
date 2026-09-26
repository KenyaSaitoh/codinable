package pro.kensait.db.connection;
/** JDBC URLの教材用表現です */
public record JdbcUrl(String subprotocol, String subname) {
    // JDBCURLの解析
    public static JdbcUrl parse(String value) {
        if (value == null || !value.startsWith("jdbc:")) {
            throw new IllegalArgumentException("JDBC URL must start with jdbc:");
        }
        int separator = value.indexOf(':', 5);
        if (separator <= 5 || separator == value.length() - 1) {
            throw new IllegalArgumentException("JDBC URL must contain a subprotocol and subname");
        }
        return new JdbcUrl(value.substring(5, separator), value.substring(separator + 1));
    }
}
