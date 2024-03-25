package accounttransaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import accounttransaction.entities.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}