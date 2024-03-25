package accounttransaction.business.abstracts;

import accounttransaction.business.dto.requests.create.CreateUserRequest;
import accounttransaction.business.dto.requests.update.UpdateUserRequest;
import accounttransaction.business.dto.responses.create.CreateUserResponse;
import accounttransaction.business.dto.responses.create.LoginUserResponse;
import accounttransaction.business.dto.responses.get.GetAllUsersResponse;
import accounttransaction.business.dto.responses.get.GetUserResponse;
import accounttransaction.business.dto.responses.update.UpdateUserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<GetAllUsersResponse> getAll();

    GetUserResponse getById(UUID id);

    CreateUserResponse add(CreateUserRequest todo);
    LoginUserResponse login(CreateUserRequest todo);

    UpdateUserResponse update(UUID id, UpdateUserRequest todo);

    void delete(UUID id);
}
