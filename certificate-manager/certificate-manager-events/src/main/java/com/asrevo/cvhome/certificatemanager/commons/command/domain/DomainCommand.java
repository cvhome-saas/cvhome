package com.asrevo.cvhome.certificatemanager.commons.command.domain;

import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.commons.command.Command;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class DomainCommand implements Command {
    private Domain domain;

    @Override
    public List<String> destinations() {
        return List.of("outDomainCommands-out-0");
    }
}
