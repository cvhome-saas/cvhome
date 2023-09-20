package com.asrevo.cvhome.commons.command.order;

import com.asrevo.cvhome.commons.domain.Domain;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderCommand extends OrderCommand {
    private Domain domain;
}
