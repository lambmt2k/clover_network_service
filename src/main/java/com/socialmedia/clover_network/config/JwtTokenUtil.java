package com.socialmedia.clover_network.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

@Component
//@PropertySource("classpath:config.properties")
public class JwtTokenUtil implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${TIME.JWT_TOKEN_VALIDITY}")
    private int JWT_TOKEN_VALIDITY;


    @Autowired
    private Properties properties;

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver){
        final Claims claims = getAllClaimFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUserIDFromToken(String token){
        return getClaimFromToken(token,Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token){
        return getClaimFromToken(token,Claims::getExpiration);
    }

    //kiem tra token het han
    private boolean isTokenExpired(String token){
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    //tao token cho user
    public String generateToken(String email){
        Map<String, Object> claims = new HashMap<>(); // Tao gia tri cho payload JWT token
//        claims.put("user_id",userDetail.getUsername());

        return doGenerateToken(claims, email);
    }

    public String doGenerateToken(Map<String, Object> claims , String user_id){
        return Jwts.builder()
                .setHeaderParam("typ","JWT")
                .setClaims(claims).setSubject(user_id)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+ JWT_TOKEN_VALIDITY))
                .signWith(SignatureAlgorithm.HS512,secret).compact();
    }

    public boolean validateToken(String token, String userId){
        final String user_id= getUserIDFromToken(token);
        return (user_id.equals(userId) && !isTokenExpired(token));
    }

}
