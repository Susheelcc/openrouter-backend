package com.open.openrouter.controller;

import com.open.openrouter.dto.LoginRequest;
import com.open.openrouter.dto.LoginResponse;
import com.open.openrouter.service.AuthService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/Login")
    public LoginResponse login(@RequestBody LoginRequest request){
        return authService.login(request);
    }
}
