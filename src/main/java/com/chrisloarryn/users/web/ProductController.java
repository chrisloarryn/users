package com.chrisloarryn.users.web;

import java.util.List;
import java.util.UUID;

import com.chrisloarryn.users.service.ProductService;
import com.chrisloarryn.users.web.dto.request.CreateProductRequest;
import com.chrisloarryn.users.web.dto.request.UpdateProductRequest;
import com.chrisloarryn.users.web.dto.response.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Products")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/api/products")
    @Operation(summary = "List all products")
    public List<ProductResponse> getAll() {
        return productService.getAll();
    }

    @GetMapping("/api/products/{id}")
    @Operation(summary = "Get a product by id")
    public ProductResponse getById(@PathVariable UUID id) {
        return productService.getById(id);
    }

    @PostMapping("/api/products")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a product")
    public ProductResponse create(@Valid @RequestBody CreateProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/api/products/{id}")
    @Operation(summary = "Update a product")
    public ProductResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/api/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a product")
    public void delete(@PathVariable UUID id) {
        productService.delete(id);
    }

    @GetMapping("/api/users/{userId}/products")
    @Operation(summary = "List products created by a user")
    public List<ProductResponse> getAllByUserId(@PathVariable UUID userId) {
        return productService.getAllByUserId(userId);
    }

    @GetMapping("/api/users/{userId}/products/{productId}")
    @Operation(summary = "Get a user product by id")
    public ProductResponse getByUserId(@PathVariable UUID userId, @PathVariable UUID productId) {
        return productService.getByUserId(userId, productId);
    }
}
