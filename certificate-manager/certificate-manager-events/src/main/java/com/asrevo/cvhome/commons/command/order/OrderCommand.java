package com.asrevo.cvhome.commons.command.order;

import com.asrevo.cvhome.commons.command.Command;
import com.asrevo.cvhome.commons.domain.OrdersId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class OrderCommand implements Command {
    private OrdersId id;
}
