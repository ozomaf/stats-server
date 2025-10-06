package com.azatkhaliullin.controller;

import com.azatkhaliullin.api.ServersApi;
import com.azatkhaliullin.dto.MatchResultDto;
import com.azatkhaliullin.dto.ServerInfoDto;
import com.azatkhaliullin.dto.ServerStatsDto;
import com.azatkhaliullin.service.ServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ServerController implements ServersApi {

    private final ServerService serverService;

    @Override
    public ResponseEntity<ServerInfoDto> getServerInfo(String endpoint) {
        return serverService.getServerInfo(endpoint)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<MatchResultDto>> getMatchesSince(String endpoint, Long timestamp) {
        Instant since = Instant.ofEpochSecond(timestamp);
        List<MatchResultDto> matches = serverService.getMatchesSince(endpoint, since);
        return ResponseEntity.ok(matches);
    }

    @Override
    public ResponseEntity<List<ServerInfoDto>> getAllServersInfo() {
        List<ServerInfoDto> servers = serverService.getAllServers();
        return ResponseEntity.ok(servers);
    }

    @Override
    public ResponseEntity<ServerStatsDto> getServerStats(String endpoint) {
        return serverService.getServerStats(endpoint)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
