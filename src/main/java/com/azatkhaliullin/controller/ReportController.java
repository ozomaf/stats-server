package com.azatkhaliullin.controller;

import com.azatkhaliullin.api.ReportsApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReportController implements ReportsApi {

    @Override
    public ResponseEntity<Void> recentMatches(Integer count) {
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> bestPlayers(Integer count) {
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> popularServers(Integer count) {
        return ResponseEntity.ok().build();
    }
}
