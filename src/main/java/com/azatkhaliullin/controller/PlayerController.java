package com.azatkhaliullin.controller;

import com.azatkhaliullin.api.PlayersApi;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.azatkhaliullin.dto.PlayerStatsDto;
import com.azatkhaliullin.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PlayerController implements PlayersApi {

    private final PlayerService playerService;

    @Override
    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<PlayerStatsDto> getPlayerStats(String username) {
        return playerService.getPlayerStats(username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
