package accounttransaction.business.dto.requests.login;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserRequest {
    @JsonProperty("email")
    @NonNull
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@(dominio\\.(cl|com)|gmail\\.(cl|com))$", message = "El correo debe ser del dominio dominio.cl, dominio.com, gmail.cl o gmail.com")
    @NotEmpty(message = "Email is required")
    private String email;

    @JsonProperty("password")
    @NonNull
    @NotEmpty(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\\$%\\^&\\*])(?=.{8,}).*$", 
            message = "La contraseña debe cumplir con los requisitos de seguridad: al menos 8 caracteres, una letra minúscula, una letra mayúscula, un número y un carácter especial (!@#$%^&*).")
    private String password;
}