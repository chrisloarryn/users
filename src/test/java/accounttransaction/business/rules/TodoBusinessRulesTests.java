package accounttransaction.business.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import accounttransaction.entities.UserNotFoundException;
import accounttransaction.repository.UserRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class TodoBusinessRulesTests {

	private UserBusinessRules todoBusinessRules;

	@Mock
	private UserRepository repository;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		todoBusinessRules = new UserBusinessRules(repository);
	}

	@Test
	public void testCheckIfTodoExists_WhenTodoExists_ShouldNotThrowException() {
		UUID id = UUID.randomUUID();
		given(repository.existsById(id)).willReturn(true);

		// The method should not throw an exception
		todoBusinessRules.checkIfTodoExists(id);

		verify(repository, times(1)).existsById(id);
	}

	@Test
	public void testCheckIfTodoExists_WhenTodoDoesNotExist_ShouldThrowTodoNotFoundException() {
		UUID id = UUID.randomUUID();
		given(repository.existsById(id)).willReturn(false);

		// The method should throw TodoNotFoundException
		assertThrows(UserNotFoundException.class, () -> {
			todoBusinessRules.checkIfTodoExists(id);
		});

		verify(repository, times(1)).existsById(id);
	}
}
