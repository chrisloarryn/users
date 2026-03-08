package com.chrisloarryn.users.service;

import java.util.List;
import java.util.UUID;

import com.chrisloarryn.users.web.dto.request.CreateProductRequest;
import com.chrisloarryn.users.web.dto.request.UpdateProductRequest;
import com.chrisloarryn.users.web.dto.response.ProductResponse;

public interface ProductService {

    List<ProductResponse> getAll();

    ProductResponse getById(UUID id);

    ProductResponse create(CreateProductRequest request);

    ProductResponse update(UUID id, UpdateProductRequest request);

    void delete(UUID id);

    List<ProductResponse> getAllByUserId(UUID userId);

    ProductResponse getByUserId(UUID userId, UUID productId);
}
