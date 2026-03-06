package com.open.openrouter.service;

import com.open.openrouter.dto.LoginRequest;
import com.open.openrouter.dto.LoginResponse;
import com.open.openrouter.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    public LoginResponse login(LoginRequest request){
        if("admin".equals(request.getUsername())&&"password".equals(request.getPassword())){
            String token = jwtTokenProvider.generateToken(request.getUsername());

            return new LoginResponse(token);
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }
}
