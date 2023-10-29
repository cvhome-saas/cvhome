package com.asrevo.cvhome.domaincertificatemanager.commons.command.order;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Domain;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderCommand extends OrderCommand {
    private Domain domain;
    private ChallengeValidationType validationType;
    private boolean includeSubDomains;
}
