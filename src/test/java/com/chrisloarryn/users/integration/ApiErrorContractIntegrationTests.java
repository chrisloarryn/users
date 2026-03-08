package com.chrisloarryn.users.integration;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiErrorContractIntegrationTests extends AbstractIntegrationTest {

    @Test
    void notFoundResponsesExposeTheSanitizedProblemDetailShape() throws Exception {
        AuthContext authContext = registerAuthContext("errors-user@example.com", "StrongPass1!");

        mockMvc.perform(get("/api/users/{id}", UUID.randomUUID())
                        .header("Authorization", bearerToken(authContext.token())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type", equalTo("about:blank")))
                .andExpect(jsonPath("$.title", equalTo("Not Found")))
                .andExpect(jsonPath("$.status", equalTo(404)))
                .andExpect(jsonPath("$.detail", equalTo("User not found")))
                .andExpect(jsonPath("$.errors[0]", equalTo("User not found")));
    }

    @Test
    void malformedJsonPayloadsReturnABadRequestProblemDetail() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"broken\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type", equalTo("about:blank")))
                .andExpect(jsonPath("$.title", equalTo("Bad Request")))
                .andExpect(jsonPath("$.status", equalTo(400)))
                .andExpect(jsonPath("$.detail", equalTo("Malformed request body.")))
                .andExpect(jsonPath("$.errors[0]", equalTo("Malformed request body.")));
    }

    @Test
    void beanValidationFailuresExposeAllFieldMessages() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name":" ",
                                  "email":"not-an-email",
                                  "password":" ",
                                  "phones":[]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type", equalTo("about:blank")))
                .andExpect(jsonPath("$.title", equalTo("Validation failed")))
                .andExpect(jsonPath("$.status", equalTo(400)))
                .andExpect(jsonPath("$.errors", hasItem(containsString("name"))))
                .andExpect(jsonPath("$.errors", hasItem(containsString("email"))))
                .andExpect(jsonPath("$.errors", hasItem(containsString("password"))))
                .andExpect(jsonPath("$.errors", hasItem(containsString("phones"))));
    }

    @Test
    void conflictResponsesRemainManagedDuringUpdates() throws Exception {
        AuthContext firstUser = registerAuthContext("first-user@example.com", "StrongPass1!");
        AuthContext secondUser = registerAuthContext("second-user@example.com", "StrongPass1!");

        mockMvc.perform(put("/api/users/{id}", firstUser.userId())
                        .header("Authorization", bearerToken(firstUser.token()))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Updated User",
                                  "email":"second-user@example.com",
                                  "password":"NewStrong1!",
                                  "phones":[{"number":"999999999","cityCode":"2","countryCode":"56"}]
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type", equalTo("about:blank")))
                .andExpect(jsonPath("$.title", equalTo("Conflict")))
                .andExpect(jsonPath("$.status", equalTo(409)))
                .andExpect(jsonPath("$.detail", equalTo("Email already registered")))
                .andExpect(jsonPath("$.errors[0]", equalTo("Email already registered")));
    }

    @Test
    void invalidPathParametersReturnABadRequestProblemDetail() throws Exception {
        AuthContext authContext = registerAuthContext("path-user@example.com", "StrongPass1!");

        mockMvc.perform(get("/api/users/not-a-uuid")
                        .header("Authorization", bearerToken(authContext.token())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type", equalTo("about:blank")))
                .andExpect(jsonPath("$.title", equalTo("Bad Request")))
                .andExpect(jsonPath("$.status", equalTo(400)))
                .andExpect(jsonPath("$.detail", equalTo("Invalid path or query parameter value.")))
                .andExpect(jsonPath("$.errors[0]", equalTo("Invalid path or query parameter value.")));
    }
}
