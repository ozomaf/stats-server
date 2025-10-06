package com.azatkhaliullin.service;

import com.azatkhaliullin.dto.MatchResultDto;
import com.azatkhaliullin.dto.PlayerStatsDto;
import com.azatkhaliullin.dto.ServerStatsDto;
import com.azatkhaliullin.mapper.MatchResultMapper;
import com.azatkhaliullin.repository.MatchRepository;
import com.azatkhaliullin.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final MatchRepository matchRepository;
    private final ServerRepository serverRepository;
    private final MatchResultMapper matchResultMapper;
    private final PlayerService playerService;
    private final ServerService serverService;

    public List<MatchResultDto> getRecentMatches(int count) {
        log.debug("Getting recent {} matches", count);
        return matchRepository.getRecentMatches(count).stream()
                .map(matchResultMapper::toDto)
                .toList();
    }

    public List<PlayerStatsDto> getBestPlayers(int count) {
        log.debug("Getting best {} players", count);
        return playerService.getBestPlayers(count);
    }

    public List<ServerStatsDto> getPopularServers(int count) {
        log.debug("Getting popular {} servers", count);

        return serverRepository.findAll().stream()
                .map(serverService::buildServerStats)
                .sorted(Comparator.comparing(ServerStatsDto::getTotalMatches).reversed())
                .limit(count)
                .toList();
    }
}
