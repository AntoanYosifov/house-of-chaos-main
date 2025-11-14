package com.antdevrealm.housechaosmain.util;

import com.antdevrealm.housechaosmain.role.model.entity.RoleEntity;
import com.antdevrealm.housechaosmain.role.model.enums.UserRole;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import com.antdevrealm.housechaosmain.user.web.dto.UserResponseDTO;

import java.util.List;

public class UserResponseDTOMapper {

    public static UserResponseDTO mapToUserResponseDTO(UserEntity userEntity) {
        List<UserRole> roles = userEntity.getRoles().stream().map(RoleEntity::getRole).toList();

        return new UserResponseDTO(userEntity.getId(),
                userEntity.getEmail(),
                userEntity.getCreatedOn(),
                userEntity.getUpdatedAt(),
                roles);
    }
}
