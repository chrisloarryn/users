package com.chrisloarryn.users.service.impl;

import java.util.List;
import java.util.UUID;

import com.chrisloarryn.users.domain.Product;
import com.chrisloarryn.users.domain.User;
import com.chrisloarryn.users.error.NotFoundException;
import com.chrisloarryn.users.repository.ProductRepository;
import com.chrisloarryn.users.service.CurrentUserService;
import com.chrisloarryn.users.service.ProductMapper;
import com.chrisloarryn.users.service.ProductService;
import com.chrisloarryn.users.web.dto.request.CreateProductRequest;
import com.chrisloarryn.users.web.dto.request.UpdateProductRequest;
import com.chrisloarryn.users.web.dto.response.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CurrentUserService currentUserService;

    public ProductServiceImpl(
            ProductRepository productRepository,
            ProductMapper productMapper,
            CurrentUserService currentUserService) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findAllByOrderByCreatedAtAsc().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(UUID id) {
        return productMapper.toResponse(getProduct(id));
    }

    @Override
    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        User actor = currentUserService.getCurrentUser();
        Product product = new Product(normalizeName(request.name()), request.price(), actor);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse update(UUID id, UpdateProductRequest request) {
        Product product = getProduct(id);
        User actor = currentUserService.getCurrentUser();
        product.updateDetails(normalizeName(request.name()), request.price(), actor);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        productRepository.delete(getProduct(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllByUserId(UUID userId) {
        return productRepository.findAllByCreatedByIdOrderByCreatedAtAsc(userId).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getByUserId(UUID userId, UUID productId) {
        return productMapper.toResponse(productRepository.findByIdAndCreatedById(productId, userId)
                .orElseThrow(() -> new NotFoundException("Product not found for user")));
    }

    private Product getProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    private String normalizeName(String name) {
        return name.trim();
    }
}
