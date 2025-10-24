package com.task.dto;

import com.task.entity.UserRole;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validAuthenticationRequest_ShouldPassValidation() {
        // Given
        AuthenticationRequest request = new AuthenticationRequest();
        request.setUserName("testuser123");
        request.setPassword("Password123!");
        request.setEmail("test@example.com");
        request.setMobileNumber("01012345678");
        request.setRole(UserRole.USER);

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty(), "Valid request should have no validation errors");
    }

    @Test
    void userName_ShouldFailValidation_WhenTooShort() {
        // Given
        AuthenticationRequest request = createValidRequest();
        request.setUserName("a"); // Too short

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("userName") &&
            v.getMessage().contains("must be between 2 and 50 characters")));
    }

    @Test
    void userName_ShouldFailValidation_WhenTooLong() {
        // Given
        AuthenticationRequest request = createValidRequest();
        request.setUserName("a".repeat(51)); // Too long

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("userName") &&
            v.getMessage().contains("must be between 2 and 50 characters")));
    }

    @Test
    void userName_ShouldFailValidation_WhenContainsInvalidCharacters() {
        // Given
        AuthenticationRequest request = createValidRequest();
        request.setUserName("user@name!"); // Invalid characters

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("userName") &&
            v.getMessage().contains("can only contain alphanumeric characters")));
    }

    @Test
    void userName_ShouldPassValidation_WhenContainsValidCharacters() {
        // Given
        AuthenticationRequest request = createValidRequest();
        request.setUserName("user.name-123_test");

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.stream().noneMatch(v -> 
            v.getPropertyPath().toString().equals("userName")));
    }

    @Test
    void password_ShouldFailValidation_WhenBlank() {
        // Given
        AuthenticationRequest request = createValidRequest();
        request.setPassword("");

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("password") &&
            v.getMessage().contains("Password is required")));
    }

    @Test
    void password_ShouldFailValidation_WhenTooShort() {
        // Given
        AuthenticationRequest request = createValidRequest();
        request.setPassword("Pass1!");

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("password") &&
            v.getMessage().contains("must be between 8 and 100 characters")));
    }

    @Test
    void password_ShouldFailValidation_WhenMissingDigit() {
        // Given
        AuthenticationRequest request = createValidRequest();
        request.setPassword("Password!");

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("password") &&
            v.getMessage().contains("must contain at least one digit")));
    }

    @Test
    void password_ShouldFailValidation_WhenMissingLowercase() {
        // Given
        AuthenticationRequest request = createValidRequest();
        request.setPassword("PASSWORD123!");

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("password") &&
            v.getMessage().contains("must contain at least one")));
    }

    @Test
    void password_ShouldFailValidation_WhenMissingUppercase() {
        // Given
        AuthenticationRequest request = createValidRequest();
        request.setPassword("password123!");

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("password") &&
            v.getMessage().contains("must contain at least one")));
    }

    @Test
    void password_ShouldFailValidation_WhenMissingSpecialCharacter() {
        // Given
        AuthenticationRequest request = createValidRequest();
        request.setPassword("Password123");

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("password") &&
            v.getMessage().contains("must contain at least one")));
    }

    @Test
    void email_ShouldFailValidation_WhenBlank() {
        // Given
        AuthenticationRequest request = createValidRequest();
        request.setEmail("");

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("email") &&
            v.getMessage().contains("Email is required")));
    }

    @Test
    void email_ShouldFailValidation_WhenInvalidFormat() {
        // Given
        AuthenticationRequest request = createValidRequest();
        request.setEmail("invalid-email");

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("email") &&
            v.getMessage().contains("Email should be valid")));
    }

    @Test
    void email_ShouldFailValidation_WhenTooLong() {
        // Given
        AuthenticationRequest request = createValidRequest();
        request.setEmail("a".repeat(90) + "@example.com"); // Over 100 chars

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("email") &&
            v.getMessage().contains("must not exceed 100 characters")));
    }

    @Test
    void mobileNumber_ShouldFailValidation_WhenInvalidFormat() {
        // Given
        AuthenticationRequest request = createValidRequest();
        request.setMobileNumber("123456"); // Invalid format

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("mobileNumber") &&
            v.getMessage().contains("Invalid phone number format")));
    }

    @Test
    void mobileNumber_ShouldPassValidation_WhenValidFormats() {
        // Test various valid formats
        String[] validNumbers = {
            "01012345678",
            "01112345678", 
            "01212345678",
            "01512345678"
        };

        for (String number : validNumbers) {
            AuthenticationRequest request = createValidRequest();
            request.setMobileNumber(number);

            Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);
            
            assertTrue(violations.stream().noneMatch(v -> 
                v.getPropertyPath().toString().equals("mobileNumber")),
                "Number " + number + " should be valid");
        }
    }

    @Test
    void mobileNumber_ShouldFailValidation_WhenInvalidPrefix() {
        // Given
        AuthenticationRequest request = createValidRequest();
        request.setMobileNumber("02012345678"); // Invalid prefix

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("mobileNumber")));
    }

    @Test
    void mobileNumber_ShouldPassValidation_WhenNull() {
        // Given
        AuthenticationRequest request = createValidRequest();
        request.setMobileNumber(null);

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.stream().noneMatch(v -> 
            v.getPropertyPath().toString().equals("mobileNumber")),
            "Mobile number should be optional");
    }

    @Test
    void role_ShouldAcceptAllValidRoles() {
        // Test that all UserRole enum values are accepted
        for (UserRole role : UserRole.values()) {
            AuthenticationRequest request = createValidRequest();
            request.setRole(role);

            Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);
            
            assertTrue(violations.stream().noneMatch(v -> 
                v.getPropertyPath().toString().equals("role")),
                "Role " + role + " should be valid");
        }
    }

    @Test
    void authenticationRequest_ShouldHandleAllFieldsNull() {
        // Given
        AuthenticationRequest request = new AuthenticationRequest();

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        // Should have violations for required fields
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("password")));
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("email")));
    }

    private AuthenticationRequest createValidRequest() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setUserName("testuser");
        request.setPassword("Password123!");
        request.setEmail("test@example.com");
        request.setMobileNumber("01012345678");
        request.setRole(UserRole.USER);
        return request;
    }
}