package com.azatkhaliullin.controller;

import com.azatkhaliullin.api.PlayersApi;
import com.azatkhaliullin.dto.PlayerStatsDto;
import com.azatkhaliullin.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PlayerController implements PlayersApi {

    private final PlayerService playerService;

    @Override
    public ResponseEntity<PlayerStatsDto> getPlayerStats(String username) {
        return playerService.getPlayerStats(username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
