package com.azatkhaliullin.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String algorithm;
    private String jwsAlgorithm;
    private int keySize;
    private Path keysDir;
    private String publicKeyFile;
    private String privateKeyFile;
    private Long expiration;
    private String keyId;

    public Path getPrivateKeyPath() {
        return keysDir.resolve(privateKeyFile);
    }

    public Path getPublicKeyPath() {
        return keysDir.resolve(publicKeyFile);
    }
}
