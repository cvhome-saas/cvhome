package com.asrevo.cvhome.manager.commons.dto;

import com.asrevo.cvhome.commons.domain.Groups;

import java.util.List;

public record KeyCloakUserDto(String id, String username,String email, String firstName, String lastName, Boolean enabled,
                              List<Groups> groups) {
}
