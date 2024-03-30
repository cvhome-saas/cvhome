package com.asrevo.cvhome.store.core.entity.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@Embeddable
public class CredentialsReset {

    @Column(name = "RESET_CREDENTIALS_REQ", length = 256)
    private String credentialsRequest;

    @Temporal(TemporalType.DATE)
    @Column(name = "RESET_CREDENTIALS_EXP")
    private Date credentialsRequestExpiry = new Date();

}
