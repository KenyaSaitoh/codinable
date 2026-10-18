package pro.kensait.jakartaee.employee.client;

import java.net.URI;

/** コマンドライン引数、環境変数 EMPLOYEE_API_URL、既定値の順に接続先を決める */
final class ClientOptions {
    private static final String DEFAULT_URL = "http://localhost:8080/employee-api/api";

    private ClientOptions() {
    }

    static String baseUrl(String[] args) {
        if (args.length > 1) {
            throw new IllegalArgumentException("引数は社員REST APIのベースURLを1つだけ指定してください");
        }
        String url = args.length == 1 ? args[0]
                : System.getenv().getOrDefault("EMPLOYEE_API_URL", DEFAULT_URL);
        URI uri = URI.create(url);
        if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
                || uri.getHost() == null || uri.getQuery() != null || uri.getFragment() != null) {
            throw new IllegalArgumentException("接続先にはhttp(s)のベースURLを指定してください");
        }
        return url.replaceAll("/+$", "");
    }
}
