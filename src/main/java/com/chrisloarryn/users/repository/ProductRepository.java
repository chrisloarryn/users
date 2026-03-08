package com.chrisloarryn.users.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.chrisloarryn.users.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findAllByOrderByCreatedAtAsc();

    List<Product> findAllByCreatedByIdOrderByCreatedAtAsc(UUID createdById);

    Optional<Product> findByIdAndCreatedById(UUID id, UUID createdById);
}
