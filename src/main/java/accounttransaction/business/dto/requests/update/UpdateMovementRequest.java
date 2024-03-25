package accounttransaction.business.dto.requests.update;

import accounttransaction.entities.enums.AccountType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMovementRequest {
    @JsonProperty("tipo")
    @NonNull
    private AccountType transactionType = AccountType.Ahorro;

    @JsonProperty("saldoinicial")
    @NonNull
    private Double initialBalance;

    @JsonProperty("valormovimiento")
    @NonNull
    private Double transactionValue;

    @JsonProperty("estado")
    @NonNull
    private Boolean status = true;
}
