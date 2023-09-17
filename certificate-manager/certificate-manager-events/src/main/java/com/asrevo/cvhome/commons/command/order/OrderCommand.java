package com.asrevo.cvhome.commons.command.order;

import com.asrevo.cvhome.commons.command.Command;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class OrderCommand implements Command {
    private Long id;
}
