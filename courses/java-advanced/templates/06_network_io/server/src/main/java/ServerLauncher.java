import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * クライアントの ファイル実行で一緒に起動するサーバーの入口（Codinable 用。講座のコードではない）
 *
 * 引数のクラスの main を動かし、確認用のポート 55556 を別に開く。Codinable とクライアントの起動待ちが
 * 接続するのはこのポートなので、教材のサーバー（55555 / 8080）には余計な接続が行かない。
 * "S" を受け取ると終了する（同じポートのサーバーを ファイル実行するとき）
 */
public class ServerLauncher {
    private static final int LAUNCHER_PORT = 55556;

    public static void main(String[] args) throws Exception {
        Thread server = new Thread(() -> {
            try {
                Class.forName(args[0]).getMethod("main", String[].class)
                        .invoke(null, (Object) new String[0]);
            } catch (ReflectiveOperationException e) {
                (e.getCause() != null ? e.getCause() : e).printStackTrace();
                System.exit(1);
            }
        }, "main");
        server.start();
        Thread.sleep(500); // サーバーがポートをバインドするのを待つ

        try (ServerSocket ready = new ServerSocket(LAUNCHER_PORT)) {
            while (true) {
                try (Socket socket = ready.accept()) {
                    socket.setSoTimeout(1000);
                    if (socket.getInputStream().read() == 'S') System.exit(0);
                } catch (IOException ignored) {
                    // 確認の接続は何も送らずに閉じる
                }
            }
        }
    }
}
