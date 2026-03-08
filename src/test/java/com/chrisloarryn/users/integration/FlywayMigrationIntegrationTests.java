package com.chrisloarryn.users.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlywayMigrationIntegrationTests extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayCreatesUsersAndPhonesTables() {
        Integer usersTable = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'users'",
                Integer.class);
        Integer phonesTable = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'phones'",
                Integer.class);

        assertEquals(1, usersTable);
        assertEquals(1, phonesTable);
    }
}
