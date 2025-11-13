package com.azatkhaliullin.controller;

import com.azatkhaliullin.api.AuthApi;
import com.azatkhaliullin.dto.LoginRequest;
import com.azatkhaliullin.dto.LoginResponse;
import com.azatkhaliullin.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthenticationService authenticationService;

    @Override
    public ResponseEntity<LoginResponse> login(LoginRequest request) {
        LoginResponse response = authenticationService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }
}
