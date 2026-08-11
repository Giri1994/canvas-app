package org.example.canvasbackend.integration;

import org.example.canvasbackend.dto.CanvasResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CanvasApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @AfterEach
    void cleanupCanvasState() throws Exception {
        mockMvc.perform(post("/api/canvas/quit")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should complete create -> line -> fill flow end to end")
    void createLineFillFlow_shouldWorkEndToEnd() throws Exception {
        mockMvc.perform(post("/api/canvas/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"width\":6,\"height\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Canvas created"));

        mockMvc.perform(post("/api/canvas/line")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"x1\":1,\"y1\":2,\"x2\":6,\"y2\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Line drawn"));

        MvcResult fillResult = mockMvc.perform(post("/api/canvas/fill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"x\":1,\"y\":1,\"color\":\"o\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Fill applied"))
                .andReturn();

        CanvasResponse body = objectMapper.readValue(
                fillResult.getResponse().getContentAsString(),
                CanvasResponse.class
        );
        assertThat(body.canvas()).contains("oooooo");
        assertThat(body.canvas()).contains("xxxxxx");
    }
}
