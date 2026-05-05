package com.geek.threaddoctor.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.geek.threaddoctor.loganalysis.LogAnalysisService;
import com.geek.threaddoctor.loganalysis.LogAnalysisSession;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class LogAnalysisControllerTest {
    @Test
    void createsLogAnalysisSession() throws Exception {
        LogAnalysisService service = Mockito.mock(LogAnalysisService.class);
        when(service.createSession()).thenReturn(new LogAnalysisSession("LOG-1", LocalDateTime.now()));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new LogAnalysisController(service)).build();

        mockMvc.perform(post("/api/log-analysis/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("LOG-1"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }
}
