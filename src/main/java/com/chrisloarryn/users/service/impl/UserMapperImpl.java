package com.chrisloarryn.users.service.impl;

import java.util.List;

import com.chrisloarryn.users.domain.Phone;
import com.chrisloarryn.users.domain.User;
import com.chrisloarryn.users.service.UserMapper;
import com.chrisloarryn.users.web.dto.request.PhoneRequest;
import com.chrisloarryn.users.web.dto.response.PhoneResponse;
import com.chrisloarryn.users.web.dto.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhones().stream()
                        .map(this::toResponse)
                        .toList(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt(),
                user.isActive());
    }

    @Override
    public List<Phone> toPhones(List<PhoneRequest> phoneRequests) {
        return phoneRequests.stream()
                .map(request -> new Phone(request.number(), request.cityCode(), request.countryCode()))
                .toList();
    }

    private PhoneResponse toResponse(Phone phone) {
        return new PhoneResponse(
                phone.getNumber(),
                phone.getCityCode(),
                phone.getCountryCode(),
                phone.getCreatedAt());
    }
}
