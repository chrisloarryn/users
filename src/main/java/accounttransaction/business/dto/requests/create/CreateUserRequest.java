package accounttransaction.business.dto.requests.create;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    @JsonProperty("name")
    @NonNull
    @NotEmpty(message = "Name is required")
    private String name;

    @JsonProperty("email")
    @NonNull
    // Email(message = "El formato del correo electrónico es inválido")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@dominio\\.cl$", message = "El correo debe ser del dominio dominio.cl")
    @NotEmpty(message = "Email is required")
    private String email;

    @JsonProperty("password")
    @NonNull
    @NotEmpty(message = "Password is required")
    private String password;

    @JsonProperty("phones")
    @NonNull
    @NotEmpty(message = "Phones is required")
    private List<PhoneRequest> phones;
}
