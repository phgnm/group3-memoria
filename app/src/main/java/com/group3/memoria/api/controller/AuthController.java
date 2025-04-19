package com.group3.memoria.api.controller;

import com.group3.memoria.api.model.AuthRequest;
import com.group3.memoria.api.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtUtil jwtUtil;

    private final String clientId = "client";

    private final String clientSecret = "1234";

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/token")
    public String getToken(@RequestBody AuthRequest request) {
        // dummy validation
        if (clientId.equals(request.getUsername()) && clientSecret.equals(request.getPassword())) {
            return jwtUtil.generateToken(request.getUsername());
        }
        throw new RuntimeException("Invalid credentials");
    }
}
