package com.azatkhaliullin.controller;

import com.azatkhaliullin.api.JwkApi;
import com.azatkhaliullin.dto.JwksResponse;
import com.azatkhaliullin.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class JwkController implements JwkApi {

    private final JwtTokenService jwtTokenService;

    @Override
    public ResponseEntity<JwksResponse> getJwks() {
        return ResponseEntity.ok(jwtTokenService.getJwks());
    }
}
