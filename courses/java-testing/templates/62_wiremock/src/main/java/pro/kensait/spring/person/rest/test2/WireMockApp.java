package pro.kensait.spring.person.rest.test2;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;

public class WireMockApp {

    // WireMockサーバーのインスタンスを生成し、ポート8080で起動
    public static void main(String[] args) {
        WireMockServer server = new WireMockServer(8080); // ポート番号を指定
        server.start();

        // GET /personsのURLに対する疑似的な振る舞いを設定
        stubFor(get(urlPathEqualTo("/persons"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("persons.json")));

        // Codinable のプレビューで開くページ（教材のスタブとは関係ない）
        stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain; charset=UTF-8")
                        .withBody("WireMock のモックサーバーが動いています")));
        // 待ち受けを続ける。止めるときは出力欄の「停止」を押す
        System.out.println("モックサーバーを起動しました: http://localhost:8080/");
    }
}