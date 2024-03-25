package accounttransaction.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@Table(name = "movements", schema = "public")
public class Movement implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("tipo")
    @NotEmpty(message = "Tipo is required")
    @Column(name = "transaction_type", nullable = false)
    private String transactionType;

    @JsonProperty("saldoinicial")
    @Column(name = "initial_balance", nullable = false)
    private Double initialBalance;

    @JsonProperty("valormovimiento")
    @Column(name = "transaction_value", nullable = false)
    private Double transactionValue;

    @JsonProperty("numerocuenta")
    @NotNull(message = "El número de cuenta no puede ser nulo")
    @Column(name = "account_number", nullable = true)
    private String accountNumber;

    @JsonProperty("estado")
    @Column(name = "status", nullable = true)
    private Boolean status = true;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    private Date createdAt;
}
