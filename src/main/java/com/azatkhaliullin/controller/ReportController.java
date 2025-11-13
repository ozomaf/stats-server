package com.azatkhaliullin.controller;

import com.azatkhaliullin.api.ReportsApi;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "bearerAuth")
public class ReportController implements ReportsApi {

    private final ReportService reportService;

    @Override
    public ResponseEntity<List<MatchResultDto>> getRecentMatches(Integer count) {
        return ResponseEntity.ok(reportService.getRecentMatches(count));
    }

    @Override
    public ResponseEntity<List<PlayerStatsDto>> getBestPlayers(Integer count) {
        return ResponseEntity.ok(reportService.getBestPlayers(count));
    }

    @Override
    public ResponseEntity<List<ServerStatsDto>> getPopularServers(Integer count) {
        return ResponseEntity.ok(reportService.getPopularServers(count));
    }
}
