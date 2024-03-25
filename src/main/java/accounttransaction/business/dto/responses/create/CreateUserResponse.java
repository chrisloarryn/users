package accounttransaction.business.dto.responses.create;

import accounttransaction.business.dto.requests.create.PhoneRequest;
import com.fasterxml.jackson.annotation.JsonProperty;

import accounttransaction.entities.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserResponse {
//    @JsonProperty("name")
//    private String name;
//
//    @JsonProperty("email")
//    private String email;
//
//    @JsonProperty("password")
//    private String password;
//
//    @JsonProperty("phones")
//    private List<PhoneRequest> phones;
//
//    @JsonProperty("fecha_creacion")
//    private Date createdAt;

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("created")
    private Date createdAt;

    @JsonProperty("modified")
    private Date modifiedAt;

    @JsonProperty("last_login")
    private Date lastLogin;

    @JsonProperty("token")
    private UUID token;

    @JsonProperty("isactive")
    private boolean isActive;
}
