package pro.kensait.spring.calc.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/*
 * SecurityConfig の URL ごとの認可を、Cognito に接続せずに確かめるテスト
 * （Codinable 版で追加したもの。講座リポジトリのサンプルには含まれない）
 *
 * - ClientRegistrationRepository をモックに差し替え、起動時に issuer-uri へ問い合わせないようにする
 *   そのため環境変数 COGNITO_* が無くてもテストは動く
 * - oidcLogin() は「Cognito でログインを終えた利用者」を擬似的に作る
 *   CustomOidcUserService は通らないので、ADMIN 権限の有無はここで直接指定する
 *   （権限の変換そのものは CustomOidcUserServiceTest で確かめる）
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mvc;

    // Cognito のメタデータ取得を行わせないための差し替え
    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    void 未認証ならログインへリダイレクトされる() throws Exception {
        mvc.perform(get("/toInput"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void ログインエラー画面は未認証でも開ける() throws Exception {
        mvc.perform(get("/loginError"))
                .andExpect(status().isOk());
    }

    @Test
    void 認証済みなら入力画面を開ける() throws Exception {
        mvc.perform(get("/toInput").with(oidcLogin()))
                .andExpect(status().isOk());
    }

    @Test
    void ADMIN権限があれば履歴を表示できる() throws Exception {
        mvc.perform(get("/viewHistory")
                .with(oidcLogin().authorities(new SimpleGrantedAuthority("ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void ADMIN権限が無い認証済みユーザーの履歴表示は403() throws Exception {
        mvc.perform(get("/viewHistory").with(oidcLogin()))
                .andExpect(status().isForbidden());
    }

    @Test
    void 計算のGETは拒否される() throws Exception {
        mvc.perform(get("/add").with(oidcLogin()))
                .andExpect(status().isForbidden());
    }

    @Test
    void CSRFトークンの無いPOSTは403() throws Exception {
        mvc.perform(post("/add").with(oidcLogin())
                .param("param1", "10").param("param2", "20"))
                .andExpect(status().isForbidden());
    }

    @Test
    void CSRFトークン付きのPOSTなら計算して結果画面へリダイレクトする() throws Exception {
        mvc.perform(post("/add")
                .with(oidcLogin().idToken(token -> token.claim("username", "alice")))
                .with(csrf())
                .param("param1", "10").param("param2", "20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/viewResult?id=*"));
    }
}
