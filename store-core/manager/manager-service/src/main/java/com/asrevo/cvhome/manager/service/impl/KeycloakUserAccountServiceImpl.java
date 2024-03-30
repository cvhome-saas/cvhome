package com.asrevo.cvhome.manager.service.impl;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreInfo;
import com.asrevo.cvhome.commons.utils.OperationExecution;
import com.asrevo.cvhome.manager.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.manager.commons.dto.CreateUserRequestDto;
import com.asrevo.cvhome.manager.commons.dto.KeyCloakUserDto;
import com.asrevo.cvhome.manager.commons.dto.ListUsersQuery;
import com.asrevo.cvhome.manager.commons.dto.RestPasswordRequestDto;
import com.asrevo.cvhome.manager.mappers.UserRepresentationMapper;
import com.asrevo.cvhome.manager.service.UserAccountService;
import com.asrevo.cvhome.manager.utils.ErrorCodes;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.asrevo.cvhome.s2s.utils.Constants.ORG_ATTR_KEY;
import static com.asrevo.cvhome.s2s.utils.Constants.STORE_ATTR_KEY;

@Service
@RequiredArgsConstructor
public class KeycloakUserAccountServiceImpl implements UserAccountService {
    private final Keycloak keycloak;
    private final OAuth2ResourceServerProperties properties;
    private final UserRepresentationMapper userRepresentationMapper;
    private UsersResource usersResource;

    private static Optional<String> extractKey(Map<String, List<String>> attributes, String key) {
        List<String> strings = attributes.get(key);
        if (strings.size() == 1) {
            return Optional.ofNullable(strings.get(0));
        } else {
            return Optional.empty();
        }
    }

    @SneakyThrows
    @PostConstruct
    void init() {
        URI jwkSetUri = new URI(properties.getJwt().getJwkSetUri());
        String realm = jwkSetUri.getPath().split("/")[2];
        this.usersResource = keycloak.realm(realm).users();

    }

    @Override
    public List<KeyCloakUserDto> list(ListUsersQuery listUsers) {
        List<UserRepresentation> list = usersResource.searchByAttributes(listUsers.query());
        return list.stream().
                map(it -> userRepresentationMapper.toDto(it, usersResource.get(it.getId()).groups()))
                .toList();
    }

    @Override
    public KeyCloakUserDto createUser(IdentityId identityId, ManagerStoreId managerStoreId, CreateUserRequestDto createUserRequestDto) {
        try {
            UserRepresentation user = new UserRepresentation();
            user.setEnabled(true);
            user.setUsername(createUserRequestDto.username());
            Map<String, List<String>> attributes = Map.of(
                    ORG_ATTR_KEY, List.of(identityId.id()),
                    STORE_ATTR_KEY, List.of(managerStoreId.getId().toString())
            );
            user.setAttributes(attributes);
            user.setGroups(createUserRequestDto.groups().stream().map(Enum::name).toList());

            Response response = usersResource.create(user);
            String userId = CreatedResponseUtil.getCreatedId(response);
            UserRepresentation representation = usersResource.get(userId).toRepresentation();
            doResetPassword(new RestPasswordRequestDto(createUserRequestDto.password()), userId);
            return userRepresentationMapper.toDto(representation, usersResource.get(userId).groups());
        } catch (Exception e) {
            throw new OperationExecution(ErrorCodes.create_user_fail);
        }
    }

    @Override
    public void resetPassword(UserOrgStoreInfo userOrgStoreInfo, ManagerStoreId storeId, RestPasswordRequestDto passwordRequestDto, String userId) {
        UserResource userResource = usersResource.get(userId);
        UserRepresentation representation = userResource.toRepresentation();
        checkAttrAndValidate(userOrgStoreInfo, storeId, representation, () -> doResetPassword(passwordRequestDto, userId));
    }

    private void doResetPassword(RestPasswordRequestDto passwordRequestDto, String userId) {
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(true);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(passwordRequestDto.password());

        UserResource userResource = usersResource.get(userId);
        userResource.resetPassword(passwordCred);
    }

    public boolean usernameExist(String username) {
        List<UserRepresentation> userRepresentations = this.usersResource.searchByUsername(username, Boolean.TRUE);
        return !userRepresentations.isEmpty();
    }

    @Override
    public void deleteUser(UserOrgStoreInfo userOrgStoreInfo, ManagerStoreId storeId, String userId) {
        UserResource userResource = usersResource.get(userId);
        UserRepresentation representation = userResource.toRepresentation();
        checkAttrAndValidate(userOrgStoreInfo, storeId, representation, userResource::remove);
    }

    @Override
    public void enableUser(UserOrgStoreInfo userOrgStoreInfo, ManagerStoreId storeId, String userId) {
        UserResource userResource = usersResource.get(userId);
        UserRepresentation representation = userResource.toRepresentation();
        checkAttrAndValidate(userOrgStoreInfo, storeId, representation, () -> {
            representation.setEnabled(Boolean.TRUE);
            userResource.update(representation);
        });
    }

    @Override
    public void disableUser(UserOrgStoreInfo userOrgStoreInfo, ManagerStoreId storeId, String userId) {
        UserResource userResource = usersResource.get(userId);
        UserRepresentation representation = userResource.toRepresentation();
        checkAttrAndValidate(userOrgStoreInfo, storeId, representation, () -> {
            representation.setEnabled(Boolean.FALSE);
            userResource.update(representation);
        });
    }

    /**
     * used to check if a user in a specific org and store
     *
     * @param userOrgStoreInfo
     * @param storeId
     * @param representation
     * @param runnable
     */

    private void checkAttrAndValidate(UserOrgStoreInfo userOrgStoreInfo, ManagerStoreId storeId, UserRepresentation representation, Runnable runnable) {
        if (attrMatch(representation, userOrgStoreInfo, storeId)) {
            runnable.run();
        } else {
            throw new OperationExecution(ErrorCodes.NOT_ALLOWED_TO_ACCESS_THIS_ORG_AND_STORE);
        }
    }

    private boolean attrMatch(UserRepresentation representation, UserOrgStoreInfo userOrgStoreInfo, ManagerStoreId storeId) {
        String orgAttr = extractKey(representation.getAttributes(), ORG_ATTR_KEY).orElseThrow(() -> new OperationExecution(ErrorCodes.KEYCLOAK_USER_ATTR_NOT_CONTAIN_ORG));
        if (!orgAttr.equals(userOrgStoreInfo.org().id())) {
            return false;
        }
        String storeAttr = extractKey(representation.getAttributes(), STORE_ATTR_KEY).orElseThrow(() -> new OperationExecution(ErrorCodes.KEYCLOAK_USER_ATTR_NOT_CONTAIN_STORE));
        if (!userOrgStoreInfo.store().equals("*")) {
            if (!storeAttr.equals(userOrgStoreInfo.store())) {
                return false;
            }
            return storeId.getId().toString().equals(userOrgStoreInfo.store());
        } else {
            return storeAttr.equals(storeId.getId().toString());
        }
    }

}

