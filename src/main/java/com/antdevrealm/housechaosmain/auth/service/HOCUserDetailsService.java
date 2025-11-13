package com.antdevrealm.housechaosmain.auth.service;

import com.antdevrealm.housechaosmain.auth.model.HOCUserDetails;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import com.antdevrealm.housechaosmain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HOCUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public HOCUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with email: " + username + " not found!"));

        return mapToUserDetails(userEntity);
    }

    private UserDetails mapToUserDetails(UserEntity userEntity) {

        List<SimpleGrantedAuthority> authorities = userEntity.getRoles().stream()
                .map(roleEntity -> {
                    String name = "ROLE_" + roleEntity.getRole().name();
                    return new SimpleGrantedAuthority(name);
                }).toList();

        return new HOCUserDetails(userEntity.getId(), userEntity.getEmail(), userEntity.getPassword(), authorities);
    }
}
