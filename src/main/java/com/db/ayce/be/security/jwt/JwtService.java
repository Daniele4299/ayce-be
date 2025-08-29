package com.db.ayce.be.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.db.ayce.be.entity.Sessione;
import com.db.ayce.be.entity.Utente;
import com.db.ayce.be.utils.Constants;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(Utente utente) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", utente.getUsername());
        claims.put("role", Constants.getRoleName(utente.getLivello()));
        return createToken(claims, jwtExpiration);
    }

    public String generateClientToken(Sessione sessione) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", Constants.ROLE_CLIENT);
        claims.put("tavoloId", sessione.getTavolo().getId());
        claims.put("tavoloNum", sessione.getTavolo().getNumero());
        claims.put("sessioneId", sessione.getId());
        claims.put("isAyce", sessione.getIsAyce());
        return createToken(claims, jwtExpiration);
    }

    private String createToken(Map<String, Object> claims, long expirationMillis) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractAllClaims(token).getSubject().equals(userDetails.getUsername())
                && extractAllClaims(token).getExpiration().after(new Date());
    }
}
