package com.azatkhaliullin.service;


import com.azatkhaliullin.domain.UserCredentials;
import com.azatkhaliullin.dto.LoginResponse;
import com.azatkhaliullin.property.JwtProperties;
import com.azatkhaliullin.repository.UserCredentialsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserCredentialsRepository userCredentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;

    public LoginResponse login(String username, String password) {
        UserCredentials userCredentials = userCredentialsRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Username not found"));

        if (!passwordEncoder.matches(password, userCredentials.getHashedPassword())) {
            log.warn("Invalid password attempt for user: {}", username);
            throw new IllegalArgumentException("Invalid login or password");
        }

        String token = jwtTokenService.generateToken(userCredentials);

        return LoginResponse.builder()
                .accessToken(token)
                .expiresIn(jwtProperties.getExpiration())
                .build();
    }
}
