package accounttransaction.business.concretes;

import accounttransaction.business.abstracts.UserService;
import accounttransaction.business.dto.responses.create.LoginUserResponse;
import accounttransaction.entities.Phone;
import accounttransaction.exceptions.BadRequestException;
import accounttransaction.exceptions.UnauthorizedException;
import lombok.AllArgsConstructor;

import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.cfg.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import accounttransaction.business.dto.requests.create.CreateUserRequest;
import accounttransaction.business.dto.requests.update.UpdateUserRequest;
import accounttransaction.business.dto.responses.create.CreateUserResponse;
import accounttransaction.business.dto.responses.get.GetAllUsersResponse;
import accounttransaction.business.dto.responses.get.GetUserResponse;
import accounttransaction.business.dto.responses.update.UpdateUserResponse;
import accounttransaction.business.rules.UserBusinessRules;
import accounttransaction.entities.User;
import accounttransaction.entities.UserNotFoundException;
import accounttransaction.repository.UserRepository;
import accounttransaction.utils.mappers.ModelMapperService;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserManager implements UserService {

    private final UserRepository repo;
    private final ModelMapperService mapper;
    private final UserBusinessRules rules;


    // read a variable from the application.properties file for password regex.

    @Override
    public List<GetAllUsersResponse> getAll() {
        var todos = repo.findAll();
        return todos
                .stream()
                .map(todo -> mapper.forResponse().map(todo, GetAllUsersResponse.class))
                .toList();
    }

    @Override
    public GetUserResponse getById(UUID id) {
        if (!repo.existsById(id)) {
            throw new UserNotFoundException(
                    "Account with id " + id + " does not exists");
        }
        var todo = repo.findById(id).orElseThrow();
        return mapper.forResponse().map(todo, GetUserResponse.class);
    }

    @Override
    public LoginUserResponse login(CreateUserRequest userRequest) {
        User user = repo.findByEmail(userRequest.getEmail()).orElse(null);
        if (user == null) {
            throw new UserNotFoundException(
                    "User with email " + userRequest.getEmail() + " does not exists");
        }
        if (!rules.validatePassword(userRequest.getPassword(), user.getPassword())) {
            throw new UnauthorizedException(
                    "Invalid password for user with email " + userRequest.getEmail());
        }
        updateTokenAndLoginInformation(user);
        User savedUser = repo.save(user);
        return mapper.forResponse().map(savedUser, LoginUserResponse.class);
    }

    @Override
    public CreateUserResponse add(CreateUserRequest userRequest) {
        // "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número."
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$";

        if (!userRequest.getEmail().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            throw new BadRequestException("El formato del correo electrónico es inválido.", HttpStatus.BAD_REQUEST.toString());
        }

        // compare if passwordRegex is not null or empty
        if (passwordRegex.isEmpty()) {
            throw new BadRequestException("No se ha configurado el regex para la contraseña.", HttpStatus.BAD_REQUEST.toString());
        }

        // validate user password with a regex
        if (!userRequest.getPassword().matches(passwordRegex)) {
            throw new BadRequestException("La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número.", HttpStatus.BAD_REQUEST.toString());
        }

        User user = repo.findByEmail(userRequest.getEmail()).orElse(null);
        boolean userExists = user != null;
        boolean isNewUser = false;
        Date now = new Date();

        if (userExists) {
            updateTokenAndRegisterInformation(user);
        } else {
            user = mapper.forRequest().map(userRequest, User.class);
            user.setCreatedAt(now);
            user.setLastLogin(now);
            user.setModifiedAt(now);
        }

        if (userRequest.getPhones() != null && !userRequest.getPhones().isEmpty()) {
            if (!userExists) {
                User finalUser = user;
                user.setPhones(userRequest.getPhones().stream()
                        .map(phoneDto -> {
                            Phone phone = mapper.forRequest().map(phoneDto, Phone.class);
                            phone.setUser(finalUser);
                            return phone;
                        }).collect(Collectors.toList()));
            } else {
                List<Phone> newPhones = userRequest.getPhones().stream()
                        .map(phoneDto -> mapper.forRequest().map(phoneDto, Phone.class))
                        .collect(Collectors.toList());
                for (Phone newPhone : newPhones) {
                    boolean phoneExists = user.getPhones().stream()
                            .anyMatch(existingPhone -> existingPhone.getNumber().equals(newPhone.getNumber())
                                    && existingPhone.getCitycode().equals(newPhone.getCitycode())
                                    && Objects.equals(existingPhone.getContrycode(), newPhone.getContrycode()));
                    if (!phoneExists) {
                        newPhone.setUser(user);
                        user.getPhones().add(newPhone);
                    }
                }
            }
        }

        User savedUser = repo.save(user);
        return mapper.forResponse().map(savedUser, CreateUserResponse.class);
    }


    private void updateTokenAndRegisterInformation(User user) {
        UUID newUUID = UUID.randomUUID();
        Date date = new Date();
        user.setToken(newUUID);
        user.setModifiedAt(date);
        user.setLastLogin(date);
    }

    private void updateTokenAndLoginInformation(User user) {
        UUID newUUID = UUID.randomUUID();
        Date date = new Date();
        user.setToken(newUUID);
        // user.setModifiedAt(date);
        user.setLastLogin(date);
    }

    @Override
    public UpdateUserResponse update(UUID id, UpdateUserRequest todoRequest) {
        if (!repo.existsById(id)) {
            throw new UserNotFoundException(
                    "Account with id " + id + " does not exists");
        }
        var todo = mapper.forRequest().map(todoRequest, User.class);
        todo.setId(id);
        repo.save(todo);
        return mapper.forResponse().map(todo, UpdateUserResponse.class);
    }

    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw new UserNotFoundException(
                    "Account with id " + id + " does not exists");
        }
        repo.deleteById(id);
    }
}
