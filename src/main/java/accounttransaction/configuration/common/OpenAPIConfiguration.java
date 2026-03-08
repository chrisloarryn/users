package accounttransaction.configuration.common;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfiguration {

    @Bean
    public OpenAPI defineOpenApi() {
        Server server = new Server();
        server.setUrl("http://localhost:8080");
        server.setDescription("Local development");

        Contact contact = new Contact();
        contact.setName("Users Management Backend");
        contact.setEmail("support@example.com");

        Info information = new Info()
                .title("Users Management API")
                .version("1.0.0")
                .description("Spring Boot backend for user registration, login, and product management.")
                .contact(contact);

        Components components = new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .info(information)
                .servers(List.of(server))
                .components(components);
    }
}
