package accounttransaction.business.dto.requests.create;

import accounttransaction.entities.User;
import accounttransaction.entities.enums.AccountType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhoneRequest {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("number")
    @NotEmpty(message = "Number is required")
    private String number;

    @JsonProperty("citycode")
    @NotEmpty(message = "City code is required")
    private String citycode;

    @JsonProperty("countrycode")
    @NotEmpty(message = "Country code is required")
    private String countrycode;

    @JsonIgnore
    private User user;
}
