package com.azatkhaliullin.controller;

import com.azatkhaliullin.api.ServersApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServerController implements ServersApi {

    @Override
    public ResponseEntity<Void> getServerInfo(String endpoint) {
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> getMatches(String endpoint, Integer timestamp) {
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> getAllServers() {
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> getServerStats(String endpoint) {
        return ResponseEntity.ok().build();
    }
}
