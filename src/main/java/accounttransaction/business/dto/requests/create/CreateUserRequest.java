package accounttransaction.business.dto.requests.create;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

import javax.validation.constraints.NotEmpty;
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
    @NotEmpty(message = "Numero is required")
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
