package com.azatkhaliullin.builder;

import com.azatkhaliullin.domain.Player;

import java.util.UUID;

import static com.azatkhaliullin.TestConstants.ID_A;
import static com.azatkhaliullin.TestConstants.USERNAME_A;

public class PlayerTestBuilder {

    private UUID id = ID_A;
    private String username = USERNAME_A;

    public static PlayerTestBuilder testPlayer() {
        return new PlayerTestBuilder();
    }

    public PlayerTestBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public PlayerTestBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public Player build() {
        return Player.builder()
                .id(id)
                .username(username)
                .build();
    }
}
