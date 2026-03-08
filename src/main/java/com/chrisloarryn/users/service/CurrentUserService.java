package com.chrisloarryn.users.service;

import java.util.UUID;

import com.chrisloarryn.users.domain.User;

public interface CurrentUserService {

    User getCurrentUser();

    UUID getCurrentUserId();
}
