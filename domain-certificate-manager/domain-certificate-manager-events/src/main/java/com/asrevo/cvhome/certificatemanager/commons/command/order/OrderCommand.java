package com.asrevo.cvhome.certificatemanager.commons.command.order;

import com.asrevo.cvhome.certificatemanager.commons.domain.OrdersId;
import com.asrevo.cvhome.commons.command.Command;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class OrderCommand implements Command {
    private OrdersId id;

    @Override
    public List<String> destinations() {
        return List.of("outOrderCommands-out-0");
    }
}
