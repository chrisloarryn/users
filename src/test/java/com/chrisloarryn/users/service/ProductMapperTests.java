package com.chrisloarryn.users.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.chrisloarryn.users.domain.Product;
import com.chrisloarryn.users.domain.User;
import com.chrisloarryn.users.service.impl.ProductMapperImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductMapperTests {

    private final ProductMapper productMapper = new ProductMapperImpl();

    @Test
    void mapsProductAuditFieldsIntoResponse() {
        User createdBy = new User("Creator", "creator@example.com", "encoded");
        User updatedBy = new User("Editor", "editor@example.com", "encoded");
        Product product = new Product("Sample Product", new BigDecimal("12.34"), createdBy);

        UUID productId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();
        UUID updatedById = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T00:00:00Z");

        product.updateDetails("Sample Product", new BigDecimal("12.34"), updatedBy);
        ReflectionTestUtils.setField(createdBy, "id", createdById);
        ReflectionTestUtils.setField(updatedBy, "id", updatedById);
        ReflectionTestUtils.setField(product, "id", productId);
        ReflectionTestUtils.setField(product, "createdAt", createdAt);
        ReflectionTestUtils.setField(product, "updatedAt", updatedAt);

        var response = productMapper.toResponse(product);

        assertEquals(productId, response.id());
        assertEquals("Sample Product", response.name());
        assertEquals(new BigDecimal("12.34"), response.price());
        assertEquals(createdAt, response.createdAt());
        assertEquals(updatedAt, response.updatedAt());
        assertEquals(createdById, response.createdByUserId());
        assertEquals(updatedById, response.updatedByUserId());
    }
}
