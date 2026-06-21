package com.busapp.userservice.security;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Configuration
public class RsaKeyConfig {

    @Value("${jwt.private-key:}")
    private String privateKeyBase64;

    @Value("${jwt.public-key:}")
    private String publicKeyBase64;

    @Getter
    private RSAPublicKey rsaPublicKey;

    /**
     * Loads RSA key pair from configuration (base64-encoded DER).
     * Falls back to an auto-generated ephemeral pair in development when
     * jwt.private-key / jwt.public-key are not configured.
     *
     * To generate persistent keys, run: ./generate-keys.sh
     */
    @Bean
    public KeyPair rsaKeyPair() throws Exception {
        log.info("private key:{}", privateKeyBase64);
        log.info("public key:{}", publicKeyBase64);
        if (!privateKeyBase64.isBlank() && !publicKeyBase64.isBlank()) {
            KeyFactory kf = KeyFactory.getInstance("RSA");

            byte[] privBytes = Base64.getMimeDecoder().decode(privateKeyBase64.trim());
            RSAPrivateKey privateKey = (RSAPrivateKey) kf.generatePrivate(
                    new PKCS8EncodedKeySpec(privBytes));

            byte[] pubBytes = Base64.getMimeDecoder().decode(publicKeyBase64.trim());
            rsaPublicKey = (RSAPublicKey) kf.generatePublic(
                    new X509EncodedKeySpec(pubBytes));

            log.info("JWT: loaded RSA key pair from configuration.");
            return new KeyPair(rsaPublicKey, privateKey);
        }

        // Fallback: auto-generate ephemeral key pair for local development only.
        // Tokens will be invalidated on every restart.
        log.warn("JWT: jwt.private-key / jwt.public-key not configured — " +
                 "generating ephemeral RSA key pair. DO NOT use this in production.");
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        rsaPublicKey = (RSAPublicKey) pair.getPublic();

        return pair;
    }
}
