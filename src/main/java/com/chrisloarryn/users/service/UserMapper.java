package com.chrisloarryn.users.service;

import java.util.List;

import com.chrisloarryn.users.domain.Phone;
import com.chrisloarryn.users.domain.User;
import com.chrisloarryn.users.web.dto.request.PhoneRequest;
import com.chrisloarryn.users.web.dto.response.UserResponse;

public interface UserMapper {

    UserResponse toResponse(User user);

    List<Phone> toPhones(List<PhoneRequest> phoneRequests);
}
