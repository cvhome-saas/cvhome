package com.asrevo.cvhome.manager.commons.dto;

import com.asrevo.cvhome.commons.domain.Country;
import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.Phone;

public record CreateManagerStoreRequest(String name, Phone phone, Country country, Email email) {}
