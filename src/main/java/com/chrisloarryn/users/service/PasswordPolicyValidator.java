package com.chrisloarryn.users.service;

public interface PasswordPolicyValidator {

    boolean isValid(String password);
}
