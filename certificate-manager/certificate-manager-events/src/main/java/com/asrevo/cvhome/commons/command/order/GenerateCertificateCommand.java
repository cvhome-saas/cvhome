package com.asrevo.cvhome.commons.command.order;

import com.asrevo.cvhome.commons.domain.OrdersId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateCertificateCommand extends OrderCommand {
    private OrdersId ordersId;
}
