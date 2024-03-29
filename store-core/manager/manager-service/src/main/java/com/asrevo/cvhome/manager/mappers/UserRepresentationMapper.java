package com.asrevo.cvhome.manager.mappers;

import com.asrevo.cvhome.commons.domain.Groups;
import com.asrevo.cvhome.manager.commons.dto.KeyCloakUserDto;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface UserRepresentationMapper {
    @Mapping(target = "groups", ignore = true)
    KeyCloakUserDto toDto(UserRepresentation representation);

    default KeyCloakUserDto toDto(UserRepresentation representation, List<GroupRepresentation> groupRepresentations) {
        KeyCloakUserDto dto = toDto(representation);
        return new KeyCloakUserDto(dto.id(), dto.username(), dto.email(), dto.firstName(), dto.lastName(), dto.enabled(), extractGroups(groupRepresentations));
    }

    default List<KeyCloakUserDto> toDto(List<UserRepresentation> representations) {
        return representations.stream().map(this::toDto).toList();
    }

    default List<Groups> extractGroups(List<GroupRepresentation> groupRepresentations) {
        return groupRepresentations.stream().map(GroupRepresentation::getName)
                .map(Groups::parse)
                .filter(Objects::nonNull)
                .toList();
    }

    default KeyCloakUserDto userWithGroups(KeyCloakUserDto it, List<GroupRepresentation> groupRepresentations) {
        List<Groups> groups = extractGroups(groupRepresentations);
        return new KeyCloakUserDto(it.id(), it.username(), it.email(), it.firstName(), it.lastName(), it.enabled(), groups);
    }

}
