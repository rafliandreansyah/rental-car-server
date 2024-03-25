package com.rentalcar.server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class JwtService {
    // mengambil key pada application.properties
    @Value("${application.jwt.key}")
    private String SECRET_KEY;

    // mengambil expiration pada application.properties
    @Value("${application.jwt.expiration}")
    private Long EXPIRATION_DATE;

    // Mendapatkan username dari token JWT
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Memvalidasi token JWT
    public Boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // Generate token JWT dari UserDetails
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // Membuat token JWT berdasarkan klaim (claims) dan username
    public String generateToken(Map<String, Object> claims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // Expired date dalam jam
                .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(EXPIRATION_DATE)))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    // Memeriksa apakah token sudah kadaluarsa
    private Boolean isTokenExpired(String token) {
        return getExpirationDate(token).before(new Date());
    }

    // Mendapatkan tanggal kadaluarsa dari token JWT
    private Date getExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Mengambil informasi klaim dari token JWT
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Mengambil semua klaim dari token JWT
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Membuat key yang digenerate dari byte
    private Key getSignInKey() {
        byte[] keyByte = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyByte);
    }
}
