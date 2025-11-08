package com.antdevrealm.housechaosmain.infrastructure.security.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.UUID;

@Getter
public class HOCUserDetails extends User {

    private UUID id;
    private String email;

    public HOCUserDetails(String username,
                          String password,
                          Collection<? extends GrantedAuthority> authorities,
                          UUID id,
                          String email) {
        super(username, password, authorities);

        this.id = id;
        this.email = email;
    }
}
