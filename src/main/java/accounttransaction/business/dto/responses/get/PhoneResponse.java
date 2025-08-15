package accounttransaction.business.dto.responses.get;

import accounttransaction.entities.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhoneResponse {
    @JsonProperty("user_id")
    private UUID user_id;

    @JsonProperty("number")
    private String number;

    @JsonProperty("citycode")
    private String citycode;

    @JsonProperty("countrycode")
    private String countrycode;

    @JsonIgnore
    private User user;

    @JsonProperty("created_at")
    private Date createdAt;
}
