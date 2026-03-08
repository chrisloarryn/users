package com.chrisloarryn.users.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.chrisloarryn.users.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    List<User> findAllByActiveTrueOrderByCreatedAtAsc();

    Optional<User> findByIdAndActiveTrue(UUID id);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);
}
