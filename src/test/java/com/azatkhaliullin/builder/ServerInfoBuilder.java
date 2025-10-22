package com.azatkhaliullin.builder;

import com.azatkhaliullin.domain.ServerInfo;
import com.azatkhaliullin.dto.ServerInfoDto;

import static com.azatkhaliullin.TestConstants.REGION;
import static com.azatkhaliullin.TestConstants.SERVER_NAME;
import static com.azatkhaliullin.TestConstants.SERVER_US_ENDPOINT;

public class ServerInfoBuilder {

    private String endpoint = SERVER_US_ENDPOINT;
    private String name = SERVER_NAME;
    private String region = REGION;

    public static ServerInfoBuilder testServerInfo() {
        return new ServerInfoBuilder();
    }

    public ServerInfoBuilder withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public ServerInfo build() {
        return ServerInfo.builder()
                .endpoint(endpoint)
                .name(name)
                .region(region)
                .build();
    }

    public ServerInfoDto buildDto() {
        return ServerInfoDto.builder()
                .endpoint(endpoint)
                .name(name)
                .region(region)
                .build();
    }
}
