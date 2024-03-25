package accounttransaction.business.rules;

import org.springframework.stereotype.Service;

import accounttransaction.entities.UserNotFoundException;
import accounttransaction.repository.UserRepository;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Service
@AllArgsConstructor
public class UserBusinessRules {
    private final UserRepository repository;



    public void checkIfTodoExists(UUID id) {
        if (!repository.existsById(id)) {
            // log the id
            System.out.println(id);

            // TODO: BusinessException
            throw new UserNotFoundException(
                    "User with id " + id + " does not exists");
        }
    }

    public boolean validatePassword(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }


}
