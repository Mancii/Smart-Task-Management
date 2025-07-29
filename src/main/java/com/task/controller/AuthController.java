package com.task.controller;

import com.task.dto.*;
import com.task.service.AuthService;
import com.task.service.TokenService;
import com.task.service.JwtUserDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;
    private final JwtUserDetailsService jwtUserDetailsService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthenticationRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> create(@RequestBody @Valid AuthenticationRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordForm passwordForm)
            throws Exception {

        jwtUserDetailsService.resetUserPassword(passwordForm);

        return ResponseEntity.ok(new LogoutResponse("Reset Password taken Successfully"));

    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader String Authorization) {
        Authorization = Authorization.substring(7);

        try {
            return ResponseEntity.ok(tokenService.logout(Authorization));
        } catch (Exception e) {
            return ResponseEntity.ok(new ErrorResponse("invalid Token", 498));
        }

    }

    @PostMapping("/kill")
    public ResponseEntity<?> killSession(@RequestParam long id) throws Exception {

        try {
            return ResponseEntity.ok(tokenService.kill(id));
        } catch (Exception e) {
            return ResponseEntity.ok(new ErrorResponse("invalid Token", 498));
        }

    }
}












