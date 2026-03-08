package com.chrisloarryn.users.service;

import com.chrisloarryn.users.config.SecurityProperties;
import com.chrisloarryn.users.service.impl.RegexPasswordPolicyValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordPolicyValidatorTests {

    private final PasswordPolicyValidator validator =
            new RegexPasswordPolicyValidator(new SecurityProperties("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,}$"));

    @Test
    void acceptsStrongPasswords() {
        assertTrue(validator.isValid("StrongPass1!"));
    }

    @Test
    void rejectsWeakPasswords() {
        assertFalse(validator.isValid("weak"));
    }
}
