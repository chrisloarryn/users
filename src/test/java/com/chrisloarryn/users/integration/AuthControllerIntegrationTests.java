package com.chrisloarryn.users.integration;

import com.chrisloarryn.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTests extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerCreatesUsersWithATokenAndHashedPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Jane Doe",
                                  "email":"jane@example.com",
                                  "password":"StrongPass1!",
                                  "phones":[{"number":"123456789","cityCode":"1","countryCode":"56"}]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenType", equalTo("Bearer")))
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.user.email", equalTo("jane@example.com")))
                .andExpect(jsonPath("$.user.password").doesNotExist());

        var storedUser = userRepository.findByEmailIgnoreCase("jane@example.com").orElseThrow();
        org.junit.jupiter.api.Assertions.assertNotEquals("StrongPass1!", storedUser.getPasswordHash());
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        register("john@example.com");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"john@example.com",
                                  "password":"WrongPass1!"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        register("duplicate@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Jane Doe",
                                  "email":"duplicate@example.com",
                                  "password":"StrongPass1!",
                                  "phones":[{"number":"123456789","cityCode":"1","countryCode":"56"}]
                                }
                                """))
                .andExpect(status().isConflict());
    }

    private void register(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "name":"Jane Doe",
                          "email":"%s",
                          "password":"StrongPass1!",
                          "phones":[{"number":"123456789","cityCode":"1","countryCode":"56"}]
                        }
                        """.formatted(email))).andExpect(status().isCreated());
    }
}
