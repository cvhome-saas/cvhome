package com.asrevo.cvhome.manager.commons.dto;

import com.asrevo.cvhome.manager.commons.domain.Groups;

import java.util.Set;

public record CreateUserRequestDto(String username, String password, Set<Groups> groups) {

}
