package com.foxminded.timetable.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SpringdocTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void shouldDisplaySwaggerUiPage() throws Exception {

        MvcResult mvcResult = mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentAsString()).contains(
                "Swagger UI");
    }

    @Test
    void shouldProduceOpenApiJsonObject() throws Exception {

        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.openapi").value("3.0.1"))
                .andExpect(jsonPath("$.info").isNotEmpty())
                .andExpect(jsonPath("$.paths").isNotEmpty());
    }

}
