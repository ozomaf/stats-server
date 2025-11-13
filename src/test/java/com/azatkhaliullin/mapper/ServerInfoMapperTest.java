package com.azatkhaliullin.mapper;

import com.azatkhaliullin.domain.ServerInfo;
import com.azatkhaliullin.dto.ServerInfoDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.azatkhaliullin.TestConstants.REGION;
import static com.azatkhaliullin.TestConstants.SERVER_EU_ENDPOINT;
import static com.azatkhaliullin.TestConstants.SERVER_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ServerInfoMapperTest {

    @Autowired
    private ServerInfoMapper serverInfoMapper;

    @Nested
    @DisplayName("toDto")
    class ToDtoTests {

        @Test
        void shouldMapServerInfoToDto() {
            ServerInfo serverInfo = ServerInfo.builder()
                    .endpoint(SERVER_EU_ENDPOINT)
                    .name(SERVER_NAME)
                    .region(REGION)
                    .build();

            ServerInfoDto dto = serverInfoMapper.toDto(serverInfo);

            assertThat(dto).isNotNull();
            assertThat(dto.getEndpoint()).isEqualTo(SERVER_EU_ENDPOINT);
            assertThat(dto.getName()).isEqualTo(SERVER_NAME);
            assertThat(dto.getRegion()).isEqualTo(REGION);
        }

        @Test
        void shouldMapServerInfoWithNullFields() {
            ServerInfo serverInfo = ServerInfo.builder()
                    .endpoint(null)
                    .name(null)
                    .region(null)
                    .build();

            ServerInfoDto dto = serverInfoMapper.toDto(serverInfo);

            assertThat(dto).isNotNull();
            assertThat(dto.getEndpoint()).isNull();
            assertThat(dto.getName()).isNull();
            assertThat(dto.getRegion()).isNull();
        }
    }
}


