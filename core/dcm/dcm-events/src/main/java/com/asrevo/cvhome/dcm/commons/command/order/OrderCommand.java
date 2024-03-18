package com.asrevo.cvhome.dcm.commons.command.order;

import com.asrevo.cvhome.commons.command.Command;
import com.asrevo.cvhome.dcm.commons.domain.OrdersId;
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
