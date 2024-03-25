package accounttransaction.entities.enums;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public enum AccountType {
    Ahorro("Ahorro"),
    Corriente("Corriente");

    private String value;

    AccountType(String value) {
        this.value = value;
    }

    // get description
    @Override
    @JsonValue
    public String toString() {
        return value;
    }

    @JsonCreator
    public static AccountType fromValue(String value) {
        for (AccountType accountType : AccountType.values()) {
            if (accountType.value.equalsIgnoreCase(value)) {
                return accountType;
            }
        }
        return null;
    }
}