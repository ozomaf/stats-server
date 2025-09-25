package com.azatkhaliullin.controller;

import com.azatkhaliullin.api.PlayersApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlayerController implements PlayersApi {

    @Override
    public ResponseEntity<Void> getPlayerStats(String name) {
        return ResponseEntity.ok().build();
    }
}
