package accounttransaction.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@Table(name = "users", schema = "public")
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("name")
    @NotEmpty(message = "Name is required")
    @Column(name = "name", nullable = false, unique = false, updatable = true)
    private String name;

    @JsonProperty("email")
    @NotEmpty(message = "Numero is required")
    @Column(name = "email", nullable = false, unique = true, updatable = true)
    private String email;

    @JsonProperty("password")
    @NotEmpty(message = "Password is required")
    @Column(name = "password", nullable = false, unique = false, updatable = true)
    private String password;

    @JsonProperty("phones")
    @JsonManagedReference
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    private List<Phone> phones;

    // modified_at
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_at", updatable = true)
    private Date modifiedAt;

    // last_login
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_login", updatable = true)
    private Date lastLogin;

    // token
    @Column(name = "token", updatable = true)
    private UUID token = UUID.randomUUID();

    // is_active
    @Column(name = "is_active", updatable = true)
    private boolean isActive = true;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    private Date createdAt;
}
