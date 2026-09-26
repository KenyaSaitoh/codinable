package pro.kensait.leafbooks.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/*
 * indexのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IndexControllerのテスト")
class IndexControllerTest {
    
    private MockMvc mockMvc;
    private IndexController indexController;
    
    // 各テストケースで共通的な前処理
    @BeforeEach
    void setUp() {
        indexController = new IndexController();
        mockMvc = MockMvcBuilders.standaloneSetup(indexController).build();
    }
    
    // 「トップページに遷移できること」の検証
    @Test
    @DisplayName("トップページに遷移できること")
    void test_index() throws Exception {
        // 実行・検証フェーズ
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("TopPage"));
    }
}
