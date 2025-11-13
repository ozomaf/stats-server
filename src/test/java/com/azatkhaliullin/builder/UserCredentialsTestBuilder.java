package com.azatkhaliullin.builder;

import com.azatkhaliullin.domain.UserCredentials;

import java.util.UUID;

import static com.azatkhaliullin.TestConstants.HASHED_PASSWORD;
import static com.azatkhaliullin.TestConstants.ID_A;
import static com.azatkhaliullin.TestConstants.USERNAME_A;

public class UserCredentialsTestBuilder {

    private UUID playerId = ID_A;
    private String username = USERNAME_A;
    private String hashedPassword = HASHED_PASSWORD;

    public static UserCredentialsTestBuilder testUserCredentials() {
        return new UserCredentialsTestBuilder();
    }

    public UserCredentialsTestBuilder withPlayerId(UUID playerId) {
        this.playerId = playerId;
        return this;
    }

    public UserCredentialsTestBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public UserCredentialsTestBuilder withHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
        return this;
    }

    public UserCredentials build() {
        return UserCredentials.builder()
                .playerId(playerId)
                .username(username)
                .hashedPassword(hashedPassword)
                .build();
    }
}
