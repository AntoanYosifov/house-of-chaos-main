package com.antdevrealm.housechaosmain.role.service;

import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.role.model.entity.RoleEntity;
import com.antdevrealm.housechaosmain.role.model.enums.UserRole;
import com.antdevrealm.housechaosmain.role.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public RoleEntity getByRole(UserRole role) {
        return this.roleRepository.findByRole(role).orElseThrow(() -> new ResourceNotFoundException(String.format("Role: %s not found", role.name())));
    }
}
