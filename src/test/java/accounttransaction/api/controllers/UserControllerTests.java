package accounttransaction.api.controllers;

import accounttransaction.api.controllers.concretes.UserControllerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import accounttransaction.business.abstracts.UserService;
import accounttransaction.business.dto.requests.create.CreateUserRequest;
import accounttransaction.business.dto.requests.update.UpdateUserRequest;
import accounttransaction.business.dto.responses.create.CreateUserResponse;
import accounttransaction.business.dto.responses.get.GetAllUsersResponse;
import accounttransaction.business.dto.responses.get.GetUserResponse;
import accounttransaction.business.dto.responses.update.UpdateUserResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class UserControllerTests {

    private UserControllerImpl todoController;

    @Mock
    private UserService todoService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        todoController = new UserControllerImpl(todoService);
    }

    @Test
    public void testGetAll() {
        List<GetAllUsersResponse> todos = new ArrayList<>();
        given(todoService.getAll()).willReturn(todos);

        List<GetAllUsersResponse> result = todoController.getAll();

        assertEquals(todos, result);
    }

    @Test
    public void testGetById() {
        UUID id = UUID.randomUUID();
        GetUserResponse todoResponse = new GetUserResponse();
        given(todoService.getById(id)).willReturn(todoResponse);

        GetUserResponse result = todoController.getById(id);

        assertEquals(todoResponse, result);
    }

    @Test
    public void testAdd() throws InterruptedException {
        CreateUserRequest request = new CreateUserRequest();
        CreateUserResponse response = new CreateUserResponse();
        given(todoService.add(request)).willReturn(response);

        CreateUserResponse result = todoController.add(request);

        assertEquals(response, result);

        // Verify that the service method was called
        verify(todoService, times(1)).add(request);

        // Verify that the service method was called with the correct argument
        verify(todoService, times(1)).add(request);

        // Verify that the service method was not called with the wrong argument
        verify(todoService, never()).add(null);

        // Verify that the service method was called with the correct argument only once
        verify(todoService, times(1)).add(request);
    }

    @Test
    public void shouldUpdate() {
        UUID id = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest();
        UpdateUserResponse response = new UpdateUserResponse();
        given(todoService.update(id, request)).willReturn(response);

        UpdateUserResponse result = todoController.update(id, request);

        assertEquals(response, result);

        // Verify that the service method was called
        verify(todoService, times(1)).update(id, request);

        // Verify that the service method was called with the correct argument
        verify(todoService, times(1)).update(id, request);

    }

    @Test
    public void shouldDelete() {
        UUID id = UUID.randomUUID();
        doNothing().when(todoService).delete(id);

        todoController.delete(id);

        // Verify that the service method was called
        verify(todoService, times(1)).delete(id);

        // Verify that the service method was called with the correct argument
        verify(todoService, times(1)).delete(id);
    }
}
