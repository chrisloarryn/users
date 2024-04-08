package accounttransaction.api.controllers.concretes;

import accounttransaction.api.controllers.abstracts.UserController;
import accounttransaction.business.abstracts.UserService;
import accounttransaction.business.dto.responses.create.LoginUserResponse;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import accounttransaction.business.dto.requests.create.CreateUserRequest;
import accounttransaction.business.dto.requests.update.UpdateUserRequest;
import accounttransaction.business.dto.responses.create.CreateUserResponse;
import accounttransaction.business.dto.responses.get.GetAllUsersResponse;
import accounttransaction.business.dto.responses.get.GetUserResponse;
import accounttransaction.business.dto.responses.update.UpdateUserResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class UserControllerImpl implements UserController {
    private final UserService service;

    public UserControllerImpl(UserService service) {
        this.service = service;
    }

    @Override
    public List<GetAllUsersResponse> getAll() {
        return service.getAll();
    }

    @Override
    public GetUserResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @Override
    public CreateUserResponse add(@Valid @RequestBody CreateUserRequest request) {
        return service.add(request);
    }

    @Override
    public LoginUserResponse login(@Valid @RequestBody CreateUserRequest request) {
        return service.login(request);
    }

    @Override
    public UpdateUserResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return service.update(id, request);
    }

    @Override
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
