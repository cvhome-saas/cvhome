package com.asrevo.cvhome.dcm.commons.command.order;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.dcm.commons.domain.ChallengeValidationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderCommand extends OrderCommand {
    private Domain domain;
    private ChallengeValidationType validationType;
    private boolean includeSubDomains;
}
