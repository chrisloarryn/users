package com.chrisloarryn.users.repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

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
class UserRepositoryDataJpaTests {

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
    void emailMustRemainUniqueAtTheDatabaseLevel() {
        userRepository.saveAndFlush(new User("Jane Doe", "jane@example.com", "encoded"));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(new User("Jane Clone", "jane@example.com", "encoded")));
    }

    @Test
    void activeUserQueryFiltersInactiveRowsAndOrdersByCreationTime() {
        User lateUser = userRepository.saveAndFlush(new User("Late User", "late@example.com", "encoded"));
        User inactiveUser = userRepository.saveAndFlush(new User("Inactive User", "inactive@example.com", "encoded"));
        inactiveUser.deactivate();
        userRepository.saveAndFlush(inactiveUser);
        User earlyUser = userRepository.saveAndFlush(new User("Early User", "early@example.com", "encoded"));

        updateCreationTime("users", earlyUser.getId().toString(), Instant.parse("2026-03-08T00:00:00Z"));
        updateCreationTime("users", lateUser.getId().toString(), Instant.parse("2026-03-08T00:05:00Z"));
        updateCreationTime("users", inactiveUser.getId().toString(), Instant.parse("2026-03-08T00:03:00Z"));

        List<User> users = userRepository.findAllByActiveTrueOrderByCreatedAtAsc();

        assertEquals(List.of("early@example.com", "late@example.com"), users.stream().map(User::getEmail).toList());
    }

    private void updateCreationTime(String tableName, String id, Instant createdAt) {
        jdbcTemplate.update(
                "UPDATE " + tableName + " SET created_at = ?, updated_at = ? WHERE id = ?",
                Timestamp.from(createdAt),
                Timestamp.from(createdAt),
                java.util.UUID.fromString(id));
    }
}
