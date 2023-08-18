package org.example.model.jwt;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

@Getter
@Setter
public class JwtAuthentication implements Authentication {
    private boolean authenticated;
    private String username;
    private String fullName;
    private List<GrantedAuthority> privileges;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return privileges; } //privileges;

    @Override
    public Object getCredentials() { return null; }

    @Override
    public Object getDetails() { return null; }

    @Override
    public Object getPrincipal() { return username; }

    @Override
    public boolean isAuthenticated() { return authenticated; }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() { return fullName; }
}
