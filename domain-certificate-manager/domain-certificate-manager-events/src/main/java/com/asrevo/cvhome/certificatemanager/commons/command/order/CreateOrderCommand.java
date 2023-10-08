package com.asrevo.cvhome.certificatemanager.commons.command.order;

import com.asrevo.cvhome.certificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderCommand extends OrderCommand {
    private Domain domain;
    private ChallengeValidationType validationType;
}
