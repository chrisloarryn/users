package com.chrisloarryn.users.service;

import java.util.List;
import java.util.UUID;

import com.chrisloarryn.users.web.dto.request.UpdateUserRequest;
import com.chrisloarryn.users.web.dto.response.UserResponse;

public interface UserService {

    List<UserResponse> getAll();

    UserResponse getById(UUID id);

    UserResponse update(UUID id, UpdateUserRequest request);

    void delete(UUID id);
}
