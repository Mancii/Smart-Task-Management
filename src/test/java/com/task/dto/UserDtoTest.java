package com.task.dto;

import com.task.entity.UserRole;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validUserDto_ShouldPassValidation() {
        // Given
        UserDto userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .mobileNumber("+1234567890")
                .enabled(true)
                .statusId(1L)
                .passwordExpiryDate(new Date())
                .build();

        // When
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        // Then
        assertTrue(violations.isEmpty(), "Valid UserDto should have no validation errors");
    }

    @Test
    void username_ShouldFailValidation_WhenBlank() {
        // Given
        UserDto userDto = createValidUserDto();
        userDto.setUsername("");

        // When
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("username") &&
            v.getMessage().contains("Username is required")));
    }

    @Test
    void username_ShouldFailValidation_WhenNull() {
        // Given
        UserDto userDto = createValidUserDto();
        userDto.setUsername(null);

        // When
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("username") &&
            v.getMessage().contains("Username is required")));
    }

    @Test
    void username_ShouldPassValidation_WhenValid() {
        // Given
        UserDto userDto = createValidUserDto();
        userDto.setUsername("validusername123");

        // When
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        // Then
        assertTrue(violations.stream().noneMatch(v -> 
            v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void email_ShouldFailValidation_WhenInvalidFormat() {
        // Given
        UserDto userDto = createValidUserDto();
        userDto.setEmail("invalid-email");

        // When
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("email") &&
            v.getMessage().contains("Email should be valid")));
    }

    @Test
    void email_ShouldPassValidation_WhenValidFormat() {
        // Test various valid email formats
        String[] validEmails = {
            "user@example.com",
            "test.email@domain.co.uk",
            "user+tag@example.org",
            "123@domain.com"
        };

        for (String email : validEmails) {
            UserDto userDto = createValidUserDto();
            userDto.setEmail(email);

            Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
            
            assertTrue(violations.stream().noneMatch(v -> 
                v.getPropertyPath().toString().equals("email")),
                "Email " + email + " should be valid");
        }
    }

    @Test
    void email_ShouldPassValidation_WhenNull() {
        // Given
        UserDto userDto = createValidUserDto();
        userDto.setEmail(null);

        // When
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        // Then
        assertTrue(violations.stream().noneMatch(v -> 
            v.getPropertyPath().toString().equals("email")),
            "Email should be optional in UserDto");
    }

    @Test
    void mobileNumber_ShouldFailValidation_WhenInvalidFormat() {
        // Given
        UserDto userDto = createValidUserDto();
        userDto.setMobileNumber("123"); // Too short

        // When
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("mobileNumber") &&
            v.getMessage().contains("Invalid mobile number format")));
    }

    @Test
    void mobileNumber_ShouldPassValidation_WhenValidFormats() {
        // Test various valid mobile number formats
        String[] validNumbers = {
            "1234567890",
            "+1234567890",
            "12345678901234",
            "+123456789012345"
        };

        for (String number : validNumbers) {
            UserDto userDto = createValidUserDto();
            userDto.setMobileNumber(number);

            Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
            
            assertTrue(violations.stream().noneMatch(v -> 
                v.getPropertyPath().toString().equals("mobileNumber")),
                "Mobile number " + number + " should be valid");
        }
    }

    @Test
    void mobileNumber_ShouldFailValidation_WhenTooShort() {
        // Given
        UserDto userDto = createValidUserDto();
        userDto.setMobileNumber("123456789"); // 9 digits, minimum is 10

        // When
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("mobileNumber")));
    }

    @Test
    void mobileNumber_ShouldFailValidation_WhenTooLong() {
        // Given
        UserDto userDto = createValidUserDto();
        userDto.setMobileNumber("1234567890123456"); // 16 digits, maximum is 15

        // When
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("mobileNumber")));
    }

    @Test
    void mobileNumber_ShouldFailValidation_WhenContainsLetters() {
        // Given
        UserDto userDto = createValidUserDto();
        userDto.setMobileNumber("+123abc7890");

        // When
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> 
            v.getPropertyPath().toString().equals("mobileNumber")));
    }

    @Test
    void mobileNumber_ShouldPassValidation_WhenNull() {
        // Given
        UserDto userDto = createValidUserDto();
        userDto.setMobileNumber(null);

        // When
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        // Then
        assertTrue(violations.stream().noneMatch(v -> 
            v.getPropertyPath().toString().equals("mobileNumber")),
            "Mobile number should be optional");
    }

    @Test
    void role_ShouldAcceptAllValidRoles() {
        // Test that all UserRole enum values are accepted
        for (UserRole role : UserRole.values()) {
            UserDto userDto = createValidUserDto();
            userDto.setRole(role);

            Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
            
            assertTrue(violations.stream().noneMatch(v -> 
                v.getPropertyPath().toString().equals("role")),
                "Role " + role + " should be valid");
        }
    }

    @Test
    void userDto_ShouldHandleNullValues() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setUsername("validuser"); // Only set required field

        // When
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        // Then
        // Should only fail on required fields
        assertTrue(violations.stream().noneMatch(v -> 
            v.getPropertyPath().toString().equals("email")),
            "Email should be optional");
        assertTrue(violations.stream().noneMatch(v -> 
            v.getPropertyPath().toString().equals("mobileNumber")),
            "Mobile number should be optional");
        assertTrue(violations.stream().noneMatch(v -> 
            v.getPropertyPath().toString().equals("role")),
            "Role should be optional");
    }

    @Test
    void userDto_BuilderPattern_ShouldWork() {
        // Given & When
        UserDto userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.ADMIN)
                .mobileNumber("+1234567890")
                .enabled(true)
                .statusId(2L)
                .passwordExpiryDate(new Date())
                .build();

        // Then
        assertNotNull(userDto);
        assertEquals(1L, userDto.getId());
        assertEquals("testuser", userDto.getUsername());
        assertEquals("test@example.com", userDto.getEmail());
        assertEquals(UserRole.ADMIN, userDto.getRole());
        assertEquals("+1234567890", userDto.getMobileNumber());
        assertTrue(userDto.isEnabled());
        assertEquals(2L, userDto.getStatusId());
        assertNotNull(userDto.getPasswordExpiryDate());
    }

    @Test
    void userDto_EqualsAndHashCode_ShouldWork() {
        // Given
        UserDto userDto1 = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        UserDto userDto2 = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        UserDto userDto3 = UserDto.builder()
                .id(2L)
                .username("differentuser")
                .email("different@example.com")
                .build();

        // Then
        assertEquals(userDto1, userDto2);
        assertNotEquals(userDto1, userDto3);
        assertEquals(userDto1.hashCode(), userDto2.hashCode());
        assertNotEquals(userDto1.hashCode(), userDto3.hashCode());
    }

    @Test
    void userDto_ToString_ShouldWork() {
        // Given
        UserDto userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        // When
        String toString = userDto.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("test@example.com"));
        assertTrue(toString.contains("1"));
    }

    private UserDto createValidUserDto() {
        return UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .mobileNumber("+1234567890")
                .enabled(true)
                .statusId(1L)
                .passwordExpiryDate(new Date())
                .build();
    }
}