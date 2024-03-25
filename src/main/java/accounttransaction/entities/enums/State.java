package accounttransaction.entities.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
public enum State
{
    ACTIVE("Activo"),
    INACTIVE("Inactivo");

    State(String activo) {
        this.state = activo;
    }

    private String state;
}