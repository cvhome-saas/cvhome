package com.asrevo.cvhome.certificatemanager.commons.command.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDomainCommand extends DomainCommand {
    private boolean autoRenew;
    private boolean autoOrder;

}
