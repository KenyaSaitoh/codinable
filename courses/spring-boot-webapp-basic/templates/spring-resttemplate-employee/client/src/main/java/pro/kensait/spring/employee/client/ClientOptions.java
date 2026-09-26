package pro.kensait.spring.employee.client;

import java.net.URI;

/** コマンドライン引数、環境変数、既定値の順に接続先を決める */
final class ClientOptions {
    // クライアントオプションの初期化
    private ClientOptions() {
    }

    // baseURLの実行
    static String baseUrl(String[] args) {
        if (args.length > 1) {
            throw new IllegalArgumentException("引数は社員REST APIのベースURLを1つだけ指定してください");
        }
        String url = args.length == 1 ? args[0]
                : System.getenv().getOrDefault("EMPLOYEE_API_URL", "http://localhost:8086");
        URI uri = URI.create(url);
        if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
                || uri.getHost() == null || uri.getQuery() != null || uri.getFragment() != null) {
            throw new IllegalArgumentException("接続先にはhttp(s)のベースURLを指定してください");
        }
        return url.replaceAll("/+$", "");
    }
}
