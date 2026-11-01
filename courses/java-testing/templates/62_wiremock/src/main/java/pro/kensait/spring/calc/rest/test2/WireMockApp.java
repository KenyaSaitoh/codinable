package pro.kensait.spring.calc.rest.test2;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;

public class WireMockApp {

    public static void main(String[] args) {
        WireMockServer server = new WireMockServer(8080); // ポート番号を指定
        server.start();

        stubFor(post(urlPathEqualTo("/calc/add"))
                .withRequestBody(containing("param1"))
                .withRequestBody(containing("param2"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("40.0"))); // ここでレスポンスのボディを適宜変更

        // 狭い方の条件を後から追加する（ここがハマりポイント）
        stubFor(post(urlPathEqualTo("/calc/add"))
                .withRequestBody(matchingJsonPath("$.param2", equalTo("-1000.0")))
                .willReturn(aResponse()
                        .withStatus(400)));

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