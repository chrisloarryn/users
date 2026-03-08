package com.chrisloarryn.users.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.chrisloarryn.users.domain.Product;
import com.chrisloarryn.users.domain.User;
import com.chrisloarryn.users.error.NotFoundException;
import com.chrisloarryn.users.repository.ProductRepository;
import com.chrisloarryn.users.service.impl.ProductMapperImpl;
import com.chrisloarryn.users.service.impl.ProductServiceImpl;
import com.chrisloarryn.users.web.dto.request.CreateProductRequest;
import com.chrisloarryn.users.web.dto.request.UpdateProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceImplTests {

    private ProductRepository productRepository;
    private CurrentUserService currentUserService;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        currentUserService = mock(CurrentUserService.class);
        productService = new ProductServiceImpl(productRepository, new ProductMapperImpl(), currentUserService);
    }

    @Test
    void createTrimsTheProductNameAndUsesTheAuthenticatedUserForAuditFields() {
        User actor = persistedUser("creator@example.com");
        when(currentUserService.getCurrentUser()).thenReturn(actor);
        when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            ReflectionTestUtils.setField(product, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(product, "createdAt", Instant.parse("2026-03-08T00:00:00Z"));
            ReflectionTestUtils.setField(product, "updatedAt", Instant.parse("2026-03-08T00:00:00Z"));
            return product;
        });

        var response = productService.create(new CreateProductRequest("  Sample Product  ", new BigDecimal("12.34")));

        assertEquals("Sample Product", response.name());
        assertEquals(actor.getId(), response.createdByUserId());
        assertEquals(actor.getId(), response.updatedByUserId());
    }

    @Test
    void updatePreservesTheCreatorAndChangesOnlyTheUpdatingActor() {
        User creator = persistedUser("creator@example.com");
        User editor = persistedUser("editor@example.com");
        Product product = persistedProduct("Original Product", new BigDecimal("12.34"), creator);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(currentUserService.getCurrentUser()).thenReturn(editor);
        when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = productService.update(
                product.getId(),
                new UpdateProductRequest("  Updated Product  ", new BigDecimal("99.99")));

        assertEquals("Updated Product", response.name());
        assertEquals(creator.getId(), response.createdByUserId());
        assertEquals(editor.getId(), response.updatedByUserId());
        assertSame(creator, product.getCreatedBy());
        assertSame(editor, product.getUpdatedBy());
    }

    @Test
    void deleteRejectsUnknownProducts() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.delete(productId));
    }

    @Test
    void getAllByUserIdReturnsProductsInRepositoryOrder() {
        User author = persistedUser("author@example.com");
        Product first = persistedProduct("A product", new BigDecimal("10.00"), author);
        Product second = persistedProduct("B product", new BigDecimal("11.00"), author);
        when(productRepository.findAllByCreatedByIdOrderByCreatedAtAsc(author.getId())).thenReturn(List.of(first, second));

        var products = productService.getAllByUserId(author.getId());

        assertEquals(2, products.size());
        assertEquals("A product", products.get(0).name());
        assertEquals("B product", products.get(1).name());
    }

    @Test
    void getByUserIdRejectsProductsOwnedByAnotherCreator() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        when(productRepository.findByIdAndCreatedById(productId, userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getByUserId(userId, productId));
    }

    private User persistedUser(String email) {
        User user = new User("User " + email, email, "encoded");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }

    private Product persistedProduct(String name, BigDecimal price, User creator) {
        Product product = new Product(name, price, creator);
        ReflectionTestUtils.setField(product, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(product, "createdAt", Instant.parse("2026-03-08T00:00:00Z"));
        ReflectionTestUtils.setField(product, "updatedAt", Instant.parse("2026-03-08T00:00:00Z"));
        return product;
    }
}
