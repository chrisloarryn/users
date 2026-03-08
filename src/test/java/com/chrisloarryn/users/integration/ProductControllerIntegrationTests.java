package com.chrisloarryn.users.integration;

import com.chrisloarryn.users.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerIntegrationTests extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void productEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/products")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Unauthorized Product",
                                  "price":12.34
                                }
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUsersCanManageProductsAndAuditUsersAreTracked() throws Exception {
        AuthContext author = registerUser("author@example.com", "StrongPass1!");
        AuthContext editor = registerUser("editor@example.com", "SecondPass1!");

        String createResponse = mockMvc.perform(post("/api/products")
                        .header("Authorization", bearer(author.token()))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Sample Product",
                                  "price":12.34
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", equalTo("Sample Product")))
                .andExpect(jsonPath("$.price", equalTo(12.34)))
                .andExpect(jsonPath("$.createdByUserId", equalTo(author.userId())))
                .andExpect(jsonPath("$.updatedByUserId", equalTo(author.userId())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String productId = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(get("/api/products")
                        .header("Authorization", bearer(editor.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/products/{id}", productId)
                        .header("Authorization", bearer(editor.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(productId)))
                .andExpect(jsonPath("$.createdByUserId", equalTo(author.userId())));

        mockMvc.perform(put("/api/products/{id}", productId)
                        .header("Authorization", bearer(editor.token()))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Updated Product",
                                  "price":99.99
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("Updated Product")))
                .andExpect(jsonPath("$.price", equalTo(99.99)))
                .andExpect(jsonPath("$.createdByUserId", equalTo(author.userId())))
                .andExpect(jsonPath("$.updatedByUserId", equalTo(editor.userId())));

        mockMvc.perform(get("/api/users/{userId}/products", author.userId())
                        .header("Authorization", bearer(editor.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/users/{userId}/products/{productId}", author.userId(), productId)
                        .header("Authorization", bearer(editor.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(productId)))
                .andExpect(jsonPath("$.createdByUserId", equalTo(author.userId())))
                .andExpect(jsonPath("$.updatedByUserId", equalTo(editor.userId())));

        mockMvc.perform(delete("/api/products/{id}", productId)
                        .header("Authorization", bearer(editor.token())))
                .andExpect(status().isNoContent());

        assertEquals(0, productRepository.count());
    }

    @Test
    void nestedProductDetailReturnsNotFoundWhenCreatorDoesNotMatch() throws Exception {
        AuthContext author = registerUser("creator@example.com", "StrongPass1!");
        AuthContext otherUser = registerUser("viewer@example.com", "ViewerPass1!");

        String createResponse = mockMvc.perform(post("/api/products")
                        .header("Authorization", bearer(author.token()))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Creator Product",
                                  "price":15.50
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String productId = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(get("/api/users/{userId}/products/{productId}", otherUser.userId(), productId)
                        .header("Authorization", bearer(otherUser.token())))
                .andExpect(status().isNotFound());
    }

    private AuthContext registerUser(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Api User",
                                  "email":"%s",
                                  "password":"%s",
                                  "phones":[{"number":"123456789","cityCode":"1","countryCode":"56"}]
                                }
                                """.formatted(email, password)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        return new AuthContext(root.get("accessToken").asText(), root.get("user").get("id").asText());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record AuthContext(String token, String userId) {
    }
}
