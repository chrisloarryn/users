package accounttransaction.business.concretes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;

import accounttransaction.business.abstracts.UserService;
import accounttransaction.business.dto.requests.create.CreateUserRequest;
import accounttransaction.business.dto.requests.update.UpdateUserRequest;
import accounttransaction.business.dto.responses.create.CreateUserResponse;
import accounttransaction.business.dto.responses.get.GetAllUsersResponse;
import accounttransaction.business.dto.responses.get.GetUserResponse;
import accounttransaction.business.dto.responses.update.UpdateUserResponse;
import accounttransaction.business.rules.UserBusinessRules;
import accounttransaction.configuration.mappers.ModelMapperConfig;
import accounttransaction.entities.User;
import accounttransaction.entities.UserNotFoundException;
import accounttransaction.repository.UserRepository;
import accounttransaction.utils.mappers.ModelMapperManager;
import accounttransaction.utils.mappers.ModelMapperService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
// The password regex is defined in UserManager.DEFAULT_PASSWORD_REGEX.
// To avoid duplication, ensure this value matches the constant in production code.
@SpringBootTest(properties = {
  "app.security.password.regex=" + accounttransaction.business.concretes.UserManager.DEFAULT_PASSWORD_REGEX
})
@ActiveProfiles("test")
public class UserManagerTests {

	private UserService todoService;

	@Mock
	private UserRepository todoRepository;

	@Mock
	private ModelMapperConfig modelMapperConfig;

	@Mock
	private ModelMapperService mapper;

	private ModelMapper resultMapper;
	private ModelMapper requestMapper;

	@Mock
	private UserBusinessRules todoBusinessRules;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);

		ModelMapper mapper = mock(ModelMapper.class);
		Configuration configuration = mock(Configuration.class);

		given(configuration.setAmbiguityIgnored(true)).willReturn(configuration);
		given(configuration.setMatchingStrategy(MatchingStrategies.LOOSE)).willReturn(configuration);
		given(mapper.getConfiguration()).willReturn(configuration);

		ModelMapperService modelMapperService = new ModelMapperManager(mapper);
		resultMapper = modelMapperService.forResponse();

		given(configuration.setMatchingStrategy(MatchingStrategies.STANDARD)).willReturn(configuration);
		given(mapper.getConfiguration()).willReturn(configuration);

		ModelMapperService modelMapperServiceReq = new ModelMapperManager(mapper);
		requestMapper = modelMapperServiceReq.forRequest();

		todoService = new UserManager(todoRepository, modelMapperService, todoBusinessRules);
	}

	@Test
	public void testGetAll() {
		// Arrange
		List<User> todoList = new ArrayList<>();
		given(todoRepository.findAll()).willReturn(todoList);

		// Act
		List<GetAllUsersResponse> result = todoService.getAll();

		// Assert
		assertNotNull(result);
		assertEquals(0, result.size());
		assertThat(result).isNotNull();
	}

	@Test
	public void testGetById_WhenTodoExists() {
		// Arrange
		UUID id = UUID.randomUUID();
		User todo = new User();
		GetUserResponse response = new GetUserResponse();

		given(todoRepository.existsById(id)).willReturn(true);
		given(todoRepository.findById(id)).willReturn(Optional.of(todo));
		given(resultMapper.map(eq(todo), eq(GetUserResponse.class))).willReturn(response);

		// Act
		GetUserResponse result = todoService.getById(id);

		// Assert
		assertNotNull(result);
	}

	@Test
	public void testGetById_WhenTodoNotFound() {
		// Arrange
		UUID id = UUID.randomUUID();

		// Act & Assert
		assertThrows(UserNotFoundException.class, () -> todoService.getById(id));
	}

	@Test
	public void testAdd_() {
		// Arrange
		CreateUserRequest request = new CreateUserRequest();
		request.setEmail("example@examle.com");
		request.setName("John Doe");
		request.setPassword("123456Hola**");
		request.setPhones(new ArrayList<>());
		User todo = new User();
		CreateUserResponse response = new CreateUserResponse();

		given(requestMapper.map(eq(request), eq(User.class))).willReturn(todo);
		given(todoRepository.save(any(User.class))).willReturn(todo);
		given(resultMapper.map(eq(todo), eq(CreateUserResponse.class))).willReturn(response);

		// Act
		CreateUserResponse result = todoService.add(request);

		// Assert
		assertNotNull(result);
	}

	@Test
	public void testUpdate_WhenTodoExists() {
		// Arrange
		UUID id = UUID.randomUUID();
		UpdateUserRequest request = new UpdateUserRequest();
		User todo = new User();
		UpdateUserResponse response = new UpdateUserResponse();

		given(requestMapper.map(eq(request), eq(User.class))).willReturn(todo);
		given(todoRepository.existsById(id)).willReturn(true);
		given(todoRepository.findById(id)).willReturn(Optional.of(todo));
		given(todoRepository.save(any(User.class))).willReturn(todo);
		given(resultMapper.map(eq(todo), eq(UpdateUserResponse.class))).willReturn(response);

		// Act
		UpdateUserResponse result = todoService.update(id, request);

		// Assert
		assertNotNull(result);
	}

	@Test
	public void testUpdate_WhenTodoNotFound() {
		// Arrange
		UUID id = UUID.randomUUID();
		UpdateUserRequest request = new UpdateUserRequest();

		given(todoRepository.existsById(id)).willReturn(false);
		given(todoRepository.findById(id)).willReturn(Optional.empty());

		// Act & Assert
		assertThrows(UserNotFoundException.class, () -> todoService.update(id, request));
	}

	@Test
	public void testDelete_WhenTodoExists() {
		// Arrange
		UUID id = UUID.randomUUID();
		given(todoRepository.existsById(id)).willReturn(true);

		// Act
		todoService.delete(id);

		// Assert
		assertThat(todoRepository).isNotNull();
	}

	@Test
	public void testDelete_WhenTodoNotFound() {
		// Arrange
		UUID id = UUID.randomUUID();
		given(todoRepository.existsById(id)).willReturn(false);
		given(todoRepository.findById(id)).willReturn(Optional.empty());

		// Act & Assert
		assertThrows(UserNotFoundException.class, () -> todoService.delete(id));
	}
}
