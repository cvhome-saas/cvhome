package com.asrevo.cvhome.keycloak.mappers;

import com.asrevo.cvhome.keycloak.domain.group.ReadableGroup;
import com.asrevo.cvhome.keycloak.domain.user.PersistableUser;
import com.asrevo.cvhome.keycloak.domain.user.ReadableUser;
import com.asrevo.cvhome.keycloak.domain.user.ReadableUserList;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.asrevo.cvhome.keycloak.utils.Constants.STORE_ATTR_KEY;

public interface UserRepresentationMapper {

    default ReadableUser toDto(UserRepresentation representation, List<GroupRepresentation> groups) {
        ReadableUser user = new ReadableUser();
        user.setId(representation.getId());
        user.setUserName(representation.getUsername());
        user.setFirstName(representation.getFirstName());
        user.setLastName(representation.getLastName());
        user.setActive(representation.isEnabled());
        user.setEmailAddress(representation.getEmail());
        user.setStore(extractKey(representation.getAttributes(), STORE_ATTR_KEY).orElse(null));
        user.setGroups(groupsToDto(groups));
        return user;
    }

    default List<ReadableGroup> groupsToDto(List<GroupRepresentation> groups) {
        return groups.stream().map(it -> {
            ReadableGroup readableGroup = new ReadableGroup();
            readableGroup.setName(it.getName());
            return readableGroup;
        }).toList();
    }


    default ReadableUserList toDto(List<UserRepresentation> representations, Function<UserRepresentation, List<GroupRepresentation>> groupExtractor) {
        ReadableUserList list = new ReadableUserList();
        list.setData(representations.stream().map(it -> this.toDto(it, groupExtractor.apply(it))).toList());
        return list;
    }

    default Optional<String> extractKey(Map<String, List<String>> attributes, String key) {
        List<String> strings = Optional.ofNullable(attributes).orElse(Map.of()).getOrDefault(key,List.of());
        if (strings.size() == 1) {
            return Optional.ofNullable(strings.get(0));
        } else {
            return Optional.empty();
        }
    }
    default UserRepresentation copyPersistableUser(PersistableUser persistableUser) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(persistableUser.isActive());
        user.setUsername(persistableUser.getUserName());
        user.setFirstName(persistableUser.getFirstName());
        user.setLastName(persistableUser.getLastName());
        user.setEmail(persistableUser.getEmailAddress());
        return user;
    }

}
