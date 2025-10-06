package com.azatkhaliullin.mapper;

import com.azatkhaliullin.domain.MatchResult;
import com.azatkhaliullin.dto.MatchResultDto;
import org.mapstruct.Mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface MatchResultMapper {

    MatchResultDto toDto(MatchResult matchResult);

    default OffsetDateTime map(Instant value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }
}
