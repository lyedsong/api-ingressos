
package com.projeto.apiingressos.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String segredo;

    private SecretKey obterChave() {
        return Keys.hmacShaKeyFor(
            segredo.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String gerarToken(String username) {

        long tempoExpiracao = 86400000;

        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tempoExpiracao))
                .signWith(obterChave())
                .compact();
    }

    public String extrairUsername(String token) {

        return Jwts.parser()
                .verifyWith(obterChave())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
