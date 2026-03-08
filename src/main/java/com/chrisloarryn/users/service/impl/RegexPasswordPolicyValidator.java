package com.chrisloarryn.users.service.impl;

import java.util.regex.Pattern;

import com.chrisloarryn.users.config.SecurityProperties;
import com.chrisloarryn.users.service.PasswordPolicyValidator;
import org.springframework.stereotype.Component;

@Component
public class RegexPasswordPolicyValidator implements PasswordPolicyValidator {

    private static final String DEFAULT_PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$";

    private final Pattern passwordPattern;

    public RegexPasswordPolicyValidator(SecurityProperties securityProperties) {
        String regex = securityProperties.passwordRegex();
        this.passwordPattern = Pattern.compile(regex == null || regex.isBlank() ? DEFAULT_PASSWORD_REGEX : regex);
    }

    @Override
    public boolean isValid(String password) {
        return password != null && passwordPattern.matcher(password).matches();
    }
}
