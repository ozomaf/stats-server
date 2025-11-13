package com.azatkhaliullin.config;

import com.azatkhaliullin.property.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private final JwtProperties jwtProperties;

    @Bean
    public KeyPair jwtKeyPair() {
        try {
            Path privateKeyPath = jwtProperties.getPrivateKeyPath();
            Path publicKeyPath = jwtProperties.getPublicKeyPath();

            if (Files.exists(privateKeyPath) && Files.exists(publicKeyPath)) {
                log.info("Loading RSA key pair from files");
                return loadKeyPair(privateKeyPath, publicKeyPath);
            }

            log.warn("RSA key pair not found, generating new one");
            Files.createDirectories(jwtProperties.getKeysDir());

            KeyPairGenerator generator = KeyPairGenerator.getInstance(jwtProperties.getAlgorithm());
            generator.initialize(jwtProperties.getKeySize());
            KeyPair keyPair = generator.generateKeyPair();

            saveKeyPair(keyPair, privateKeyPath, publicKeyPath);

            log.info("RSA key pair successfully generated and saved");
            return keyPair;

        } catch (IOException | GeneralSecurityException e) {
            log.error("Failed to initialize RSA key pair", e);
            throw new IllegalStateException("JWT key pair initialization failed", e);
        }
    }

    private void saveKeyPair(KeyPair keyPair, Path privateKeyPath, Path publicKeyPath) throws IOException {
        Files.write(privateKeyPath, keyPair.getPrivate().getEncoded());
        Files.write(publicKeyPath, keyPair.getPublic().getEncoded());
    }

    private KeyPair loadKeyPair(Path privateKeyPath, Path publicKeyPath) throws IOException, GeneralSecurityException {
        byte[] privateBytes = Files.readAllBytes(privateKeyPath);
        byte[] publicBytes = Files.readAllBytes(publicKeyPath);

        KeyFactory factory = KeyFactory.getInstance(jwtProperties.getAlgorithm());
        PrivateKey privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
        PublicKey publicKey = factory.generatePublic(new X509EncodedKeySpec(publicBytes));

        return new KeyPair(publicKey, privateKey);
    }
}
