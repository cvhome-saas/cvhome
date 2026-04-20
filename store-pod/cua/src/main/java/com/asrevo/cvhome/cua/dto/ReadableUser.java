package com.asrevo.cvhome.cua.dto;

import java.util.UUID;

import com.asrevo.cvhome.cua.domain.User;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReadableUser {

    private UUID id;

    private String clientId;

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private boolean enabled;

    public static ReadableUser fromUser(User user) {
        return ReadableUser.builder()
                .id(user.getId())
                .clientId(user.getClientId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.isEnabled())
                .build();
    }

}