package com.geek.threaddoctor.api;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.geek.threaddoctor.common.ApiExceptionHandler;
import com.geek.threaddoctor.llm.LlmRuntimeConfigurationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class LlmConfigurationControllerTest {
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LlmRuntimeConfigurationService service =
                new LlmRuntimeConfigurationService("https://backend.test/v1", "backend-secret-key", "backend-model");
        mockMvc = MockMvcBuilders.standaloneSetup(new LlmConfigurationController(service))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void readsBackendFallbackStatus() throws Exception {
        mockMvc.perform(get("/api/llm/configuration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeSource").value("backend"))
                .andExpect(jsonPath("$.baseUrl.value").value("https://backend.test/v1"))
                .andExpect(jsonPath("$.apiKey.value").value("back****-key"))
                .andExpect(content().string(not(containsString("backend-secret-key"))));
    }

    @Test
    void savesAndClearsFrontendConfiguration() throws Exception {
        mockMvc.perform(put("/api/llm/configuration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"baseUrl\":\"https://frontend.test/v1\",\"apiKey\":\"frontend-secret-key\",\"model\":\"frontend-model\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeSource").value("frontend"))
                .andExpect(jsonPath("$.baseUrl.source").value("frontend"))
                .andExpect(jsonPath("$.model.value").value("frontend-model"))
                .andExpect(jsonPath("$.apiKey.value").value("fron****-key"))
                .andExpect(content().string(not(containsString("frontend-secret-key"))));

        mockMvc.perform(delete("/api/llm/configuration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeSource").value("backend"))
                .andExpect(jsonPath("$.model.value").value("backend-model"));
    }

    @Test
    void rejectsInvalidConfiguration() throws Exception {
        mockMvc.perform(put("/api/llm/configuration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"baseUrl\":\"ftp://invalid.test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("baseUrl")));
    }
}
