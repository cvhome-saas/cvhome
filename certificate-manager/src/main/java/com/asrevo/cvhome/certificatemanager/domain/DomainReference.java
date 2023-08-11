package com.asrevo.cvhome.certificatemanager.domain;

import org.springframework.data.annotation.Id;

public record DomainReference(@Id Long id, String domain, String reference) {
}
