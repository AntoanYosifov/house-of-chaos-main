package com.antdevrealm.housechaosmain.util;

import com.antdevrealm.housechaosmain.address.dto.AddressResponseDTO;
import com.antdevrealm.housechaosmain.address.model.AddressEntity;
import com.antdevrealm.housechaosmain.role.model.entity.RoleEntity;
import com.antdevrealm.housechaosmain.role.model.enums.UserRole;
import com.antdevrealm.housechaosmain.user.dto.UserResponseDTO;
import com.antdevrealm.housechaosmain.user.model.UserEntity;

import java.util.List;

public final class ResponseDTOMapper {

    public static UserResponseDTO mapToUserResponseDTO(UserEntity userEntity) {
        List<UserRole> roles = userEntity.getRoles().stream().map(RoleEntity::getRole).toList();

        return new UserResponseDTO(userEntity.getId(),
                userEntity.getEmail(),
                userEntity.getFirstName(),
                userEntity.getLastName(),
                mapToAddressResponseDTO(userEntity.getAddress()),
                userEntity.getCreatedOn(),
                userEntity.getUpdatedAt(),
                roles);
    }

    public static AddressResponseDTO mapToAddressResponseDTO(AddressEntity addressEntity) {
        if(addressEntity == null) {
            return null;
        }
        return new AddressResponseDTO(addressEntity.getId(),
                addressEntity.getCountry(),
                addressEntity.getCity(),
                addressEntity.getZip(),
                addressEntity.getStreet(),
                addressEntity.getCreatedOn(),
                addressEntity.getUpdatedAt());
    }
}
