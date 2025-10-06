package com.azatkhaliullin.mapper;

import com.azatkhaliullin.domain.ServerInfo;
import com.azatkhaliullin.dto.ServerInfoDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServerInfoMapper {

    ServerInfoDto toDto(ServerInfo serverInfo);
}
