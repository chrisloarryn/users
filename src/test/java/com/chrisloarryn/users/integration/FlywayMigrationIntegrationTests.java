package com.chrisloarryn.users.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlywayMigrationIntegrationTests extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayCreatesUsersPhonesAndProductsTables() {
        Integer usersTable = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'users'",
                Integer.class);
        Integer phonesTable = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'phones'",
                Integer.class);
        Integer productsTable = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'products'",
                Integer.class);
        Integer createdByColumn = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where table_schema = 'public' and table_name = 'products' and column_name = 'created_by_user_id'",
                Integer.class);
        Integer updatedByColumn = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where table_schema = 'public' and table_name = 'products' and column_name = 'updated_by_user_id'",
                Integer.class);
        Integer productsForeignKeys = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.table_constraints where table_schema = 'public' and table_name = 'products' and constraint_type = 'FOREIGN KEY'",
                Integer.class);

        assertEquals(1, usersTable);
        assertEquals(1, phonesTable);
        assertEquals(1, productsTable);
        assertEquals(1, createdByColumn);
        assertEquals(1, updatedByColumn);
        assertEquals(2, productsForeignKeys);
    }
}
