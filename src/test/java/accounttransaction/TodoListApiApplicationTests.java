package accounttransaction;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = UsersManagementApiApplication.class)
@ActiveProfiles("test")
class ClientPersonApiApplicationTests {

	@Test
	public void contextLoads() {
		// This test method is empty because it is used to check if the Spring context
		// loads successfully.
	}
}
