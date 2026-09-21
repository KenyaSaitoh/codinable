package pro.kensait.spring.users;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/*
 * UserController のテスト
 *
 * @WebMvcTest は Web の層だけを起動する (組み込み Tomcat は使わず、
 * MockMvc が疑似的にリクエストを流す)。実際に HTTP を喋らないので速い
 *
 * Spring Boot 4 では @WebMvcTest の置き場所が
 * org.springframework.boot.webmvc.test.autoconfigure に移っている
 */
@WebMvcTest(UserController.class)
// UserService は @WebMvcTest の対象外なので、必要なものを明示して読み込む
@Import({ UserService.class, GlobalExceptionHandler.class })
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/users は一覧を返す")
    void testList() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Alice"));
    }

    @Test
    @DisplayName("GET /api/users/{id} は 1 件を返す")
    void testGet() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    @DisplayName("存在しない id は 404 を返す")
    void testGetNotFound() throws Exception {
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/users は 201 と Location を返す")
    void testCreate() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Carol\",\"email\":\"carol@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users/3"))
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    @DisplayName("決まりを満たさない本文は 400 を返す")
    void testCreateInvalid() throws Exception {
        // email が形式違反。errors に項目名が入る
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Carol\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} は 204 を返す")
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
}
