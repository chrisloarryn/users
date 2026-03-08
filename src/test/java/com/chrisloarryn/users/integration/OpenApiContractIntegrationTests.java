package com.chrisloarryn.users.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {
        "springdoc.api-docs.enabled=true",
        "springdoc.swagger-ui.enabled=true"
})
class OpenApiContractIntegrationTests extends AbstractIntegrationTest {

    @Test
    void openApiDocumentMatchesTheCurrentPathAndSchemaSnapshot() throws Exception {
        String response = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode document = objectMapper.readTree(response);

        assertEquals(
                readExpectedArray("openapi/expected-paths.json"),
                sortedObjectKeys(extractObjectBlock(response, "\"paths\"\\s*:\\s*\\{")));
        assertEquals(
                readExpectedArray("openapi/expected-schemas.json"),
                sortedObjectKeys(extractObjectBlock(response, "\"schemas\"\\s*:\\s*\\{")));
        assertEquals("http", document.path("components").path("securitySchemes").path("bearerAuth").path("type").asText());
        assertEquals("bearer", document.path("components").path("securitySchemes").path("bearerAuth").path("scheme").asText());
    }

    private List<String> readExpectedArray(String resourcePath) throws Exception {
        JsonNode node = objectMapper.readTree(new ClassPathResource(resourcePath).getInputStream());
        List<String> values = new ArrayList<>();
        for (JsonNode value : node) {
            values.add(value.asText());
        }
        return values;
    }

    private List<String> sortedObjectKeys(String objectBlock) {
        List<String> names = new ArrayList<>();
        int depth = 0;
        boolean escaping = false;
        for (int index = 0; index < objectBlock.length(); index++) {
            char current = objectBlock.charAt(index);
            if (current == '{') {
                depth++;
                continue;
            }
            if (current == '}') {
                depth--;
                continue;
            }
            if (current == '"' && !escaping && depth == 1) {
                int end = index + 1;
                while (end < objectBlock.length()) {
                    char valueChar = objectBlock.charAt(end);
                    if (valueChar == '"' && objectBlock.charAt(end - 1) != '\\') {
                        break;
                    }
                    end++;
                }
                int colonIndex = end + 1;
                while (colonIndex < objectBlock.length() && Character.isWhitespace(objectBlock.charAt(colonIndex))) {
                    colonIndex++;
                }
                if (colonIndex < objectBlock.length() && objectBlock.charAt(colonIndex) == ':') {
                    names.add(objectBlock.substring(index + 1, end));
                }
                index = end;
            }
            escaping = current == '\\' && !escaping;
        }
        Collections.sort(names);
        return names;
    }

    private String extractObjectBlock(String json, String objectPattern) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(objectPattern).matcher(json);
        if (!matcher.find()) {
            return "";
        }

        int start = matcher.end() - 1;
        int depth = 0;
        for (int index = start; index < json.length(); index++) {
            char current = json.charAt(index);
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return json.substring(start, index + 1);
                }
            }
        }

        return "";
    }
}
