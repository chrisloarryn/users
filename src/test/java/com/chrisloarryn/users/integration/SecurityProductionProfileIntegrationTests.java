package com.chrisloarryn.users.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "prod"})
class SecurityProductionProfileIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void productionProfileDoesNotExposeSwaggerButKeepsHealthEndpointsPublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }
}
