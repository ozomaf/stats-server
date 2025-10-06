package com.azatkhaliullin.controller;

import com.azatkhaliullin.api.ReportsApi;
import com.azatkhaliullin.dto.MatchResultDto;
import com.azatkhaliullin.dto.PlayerStatsDto;
import com.azatkhaliullin.dto.ServerStatsDto;
import com.azatkhaliullin.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReportController implements ReportsApi {

    private final ReportService reportService;

    @Override
    public ResponseEntity<List<MatchResultDto>> getRecentMatches(Integer count) {
        List<MatchResultDto> matches = reportService.getRecentMatches(count);
        return ResponseEntity.ok(matches);
    }

    @Override
    public ResponseEntity<List<PlayerStatsDto>> getBestPlayers(Integer count) {
        List<PlayerStatsDto> players = reportService.getBestPlayers(count);
        return ResponseEntity.ok(players);
    }

    @Override
    public ResponseEntity<List<ServerStatsDto>> getPopularServers(Integer count) {
        List<ServerStatsDto> servers = reportService.getPopularServers(count);
        return ResponseEntity.ok(servers);
    }
}
