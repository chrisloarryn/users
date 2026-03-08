package com.chrisloarryn.users.service;

import com.chrisloarryn.users.domain.Product;
import com.chrisloarryn.users.web.dto.response.ProductResponse;

public interface ProductMapper {

    ProductResponse toResponse(Product product);
}
