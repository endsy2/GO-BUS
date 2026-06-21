package com.busapp.gatewayservice.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Configuration
public class RsaKeyConfig {

    @Value("${jwt.public-key}")
    private String publicKeyBase64;

    /**
     * Loads only the RSA public key — the gateway never signs tokens.
     * Set the JWT_PUBLIC_KEY env var (or jwt.public-key in application.yml)
     * to the same base64-encoded public key produced by ./generate-keys.sh.
     */
    @Bean
    public RSAPublicKey rsaPublicKey() throws Exception {
        byte[] pubBytes = Base64.getMimeDecoder().decode(publicKeyBase64.trim());
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKey key = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(pubBytes));
        log.info("Gateway JWT: RSA public key loaded.");
        return key;
    }
}
