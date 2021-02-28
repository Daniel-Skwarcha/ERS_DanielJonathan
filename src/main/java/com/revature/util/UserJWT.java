package com.revature.util;

import com.revature.dtos.Principal;
import com.revature.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public class UserJWT {
    public static String createJwt(User subject) {

        SignatureAlgorithm sigAlg = SignatureAlgorithm.HS512;
        long now = System.currentTimeMillis();

        JwtBuilder builder = Jwts.builder()
                .setId(Integer.toString(subject.getUserId()))
                .setSubject(subject.getUsername())
                .setIssuer("revature")
                .claim("role", subject.getUserRole())
                .setExpiration(new Date(now + 300000))
                .signWith(sigAlg, "t9(5f@34%$cafT><");

        return builder.compact();
    }

    public static void checkToken(HttpServletRequest req) {


        // Get the HTTP cookie named Authorization
        String token = "";
        Cookie[] cookies = req.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("my-token")) {
                token = cookie.getValue();
            }
        }
        try {

            Claims claims = Jwts.parser()
                    .setSigningKey("t9(5f@34%$cafT><")
                    .parseClaimsJws(token)
                    .getBody();

            Principal principal = new Principal();
            principal.setId(Integer.parseInt(claims.getId()));
            principal.setUsername(claims.getSubject());
            principal.setRole(claims.get("role", String.class));

            req.setAttribute("principal", principal);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
