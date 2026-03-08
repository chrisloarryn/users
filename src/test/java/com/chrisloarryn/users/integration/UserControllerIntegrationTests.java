package com.chrisloarryn.users.integration;

import com.chrisloarryn.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIntegrationTests extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void protectedEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidJwtIsRejected() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedClientCanReadUpdateAndDeactivateUsers() throws Exception {
        String token = registerAndLogin("apiuser@example.com");
        String userId = userRepository.findByEmailIgnoreCase("apiuser@example.com").orElseThrow().getId().toString();

        mockMvc.perform(get("/api/users").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/users/{id}", userId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(userId)))
                .andExpect(jsonPath("$.email", equalTo("apiuser@example.com")));

        mockMvc.perform(put("/api/users/{id}", userId)
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Updated User",
                                  "email":"updated@example.com",
                                  "password":"NewStrong1!",
                                  "phones":[{"number":"999999999","cityCode":"2","countryCode":"56"}]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("Updated User")))
                .andExpect(jsonPath("$.email", equalTo("updated@example.com")))
                .andExpect(jsonPath("$.phones[0].number", equalTo("999999999")));

        mockMvc.perform(delete("/api/users/{id}", userId).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertFalse(userRepository.findByEmailIgnoreCase("updated@example.com").orElseThrow().isActive());
    }

    private String registerAndLogin(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "name":"Api User",
                          "email":"%s",
                          "password":"StrongPass1!",
                          "phones":[{"number":"123456789","cityCode":"1","countryCode":"56"}]
                        }
                        """.formatted(email))).andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"%s",
                                  "password":"StrongPass1!"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(loginResponse);
        return root.get("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
