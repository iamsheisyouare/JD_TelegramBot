package org.example.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.example.model.jwt.JwtAuthentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JwtUtils {

    public static JwtAuthentication generate(Claims claims) {
        final JwtAuthentication jwtInfoToken = new JwtAuthentication();
        List<String> roles = claims.get("roles", ArrayList.class);
        var privileges = roles.stream().
                map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role)).
                collect(Collectors.toList());
        jwtInfoToken.setPrivileges(privileges);
        //jwtInfoToken.setPrivileges(getPrivileges(claims));
        jwtInfoToken.setFullName(claims.get("firstName", String.class));
        jwtInfoToken.setUsername(claims.getSubject());
        return jwtInfoToken;
    }

}
