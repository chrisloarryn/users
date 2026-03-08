package com.chrisloarryn.users.repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.chrisloarryn.users.domain.Product;
import com.chrisloarryn.users.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class ProductRepositoryDataJpaTests {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM products");
        jdbcTemplate.execute("DELETE FROM phones");
        jdbcTemplate.execute("DELETE FROM users");
    }

    @Test
    void productsByCreatorAreReturnedInCreationOrder() {
        User creator = userRepository.saveAndFlush(new User("Creator", "creator@example.com", "encoded"));
        Product later = productRepository.saveAndFlush(new Product("Later Product", new BigDecimal("20.00"), creator));
        Product earlier = productRepository.saveAndFlush(new Product("Earlier Product", new BigDecimal("10.00"), creator));

        updateCreationTime(earlier.getId(), Instant.parse("2026-03-08T00:00:00Z"));
        updateCreationTime(later.getId(), Instant.parse("2026-03-08T00:05:00Z"));

        List<Product> products = productRepository.findAllByCreatedByIdOrderByCreatedAtAsc(creator.getId());

        assertEquals(List.of("Earlier Product", "Later Product"), products.stream().map(Product::getName).toList());
    }

    @Test
    void creatorScopedLookupRejectsProductsFromAnotherUser() {
        User creator = userRepository.saveAndFlush(new User("Creator", "creator@example.com", "encoded"));
        User other = userRepository.saveAndFlush(new User("Other", "other@example.com", "encoded"));
        Product product = productRepository.saveAndFlush(new Product("Creator Product", new BigDecimal("15.50"), creator));

        Optional<Product> loaded = productRepository.findByIdAndCreatedById(product.getId(), other.getId());

        assertEquals(Optional.empty(), loaded);
    }

    @Test
    void databaseRejectsProductsThatReferenceMissingUsers() {
        UUID productId = UUID.randomUUID();
        UUID missingUserId = UUID.randomUUID();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbcTemplate.update(
                        """
                        INSERT INTO products (
                          id, name, price, created_at, updated_at, created_by_user_id, updated_by_user_id
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                        productId,
                        "Broken Product",
                        new BigDecimal("12.34"),
                        Timestamp.from(Instant.parse("2026-03-08T00:00:00Z")),
                        Timestamp.from(Instant.parse("2026-03-08T00:00:00Z")),
                        missingUserId,
                        missingUserId));
    }

    private void updateCreationTime(UUID productId, Instant createdAt) {
        jdbcTemplate.update(
                "UPDATE products SET created_at = ?, updated_at = ? WHERE id = ?",
                Timestamp.from(createdAt),
                Timestamp.from(createdAt),
                productId);
    }
}
