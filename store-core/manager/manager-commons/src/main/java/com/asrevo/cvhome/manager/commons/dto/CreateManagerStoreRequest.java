package com.asrevo.cvhome.manager.commons.dto;

import com.asrevo.cvhome.commons.domain.Country;
import com.asrevo.cvhome.commons.domain.Email;

public record CreateManagerStoreRequest(String name, Country country, Email email) {
}
