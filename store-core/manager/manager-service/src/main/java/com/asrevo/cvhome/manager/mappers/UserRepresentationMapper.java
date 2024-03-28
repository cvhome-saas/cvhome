package com.asrevo.cvhome.manager.mappers;

import com.asrevo.cvhome.manager.commons.dto.KeyCloakUserDto;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserRepresentationMapper {
    @Mapping(target = "groups", ignore = true)
    KeyCloakUserDto toDto(UserRepresentation representation);

    default List<KeyCloakUserDto> toDto(List<UserRepresentation> representations) {
        return representations.stream().map(this::toDto).toList();
    }
}
