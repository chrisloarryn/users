package com.chrisloarryn.users.service.impl;

import com.chrisloarryn.users.domain.Product;
import com.chrisloarryn.users.service.ProductMapper;
import com.chrisloarryn.users.web.dto.response.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getCreatedBy().getId(),
                product.getUpdatedBy().getId());
    }
}
