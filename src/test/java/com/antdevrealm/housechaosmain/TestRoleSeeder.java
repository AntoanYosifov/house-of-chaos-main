package com.antdevrealm.housechaosmain;

import com.antdevrealm.housechaosmain.role.model.entity.RoleEntity;
import com.antdevrealm.housechaosmain.role.model.enums.UserRole;
import com.antdevrealm.housechaosmain.role.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("test")
public class TestRoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Autowired
    public TestRoleSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        if (this.roleRepository.count() == 0) {
            List<RoleEntity> roles = List.of(
                    RoleEntity.builder()
                            .role(UserRole.USER)
                            .build(),
                    RoleEntity.builder()
                            .role(UserRole.ADMIN)
                            .build()
            );

            this.roleRepository.saveAll(roles);
        }
    }
}
