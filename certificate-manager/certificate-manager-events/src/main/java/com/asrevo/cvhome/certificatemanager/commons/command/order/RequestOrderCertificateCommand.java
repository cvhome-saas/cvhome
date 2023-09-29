package com.asrevo.cvhome.certificatemanager.commons.command.order;

import com.asrevo.cvhome.certificatemanager.commons.domain.OrdersId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestOrderCertificateCommand extends OrderCommand {
    private OrdersId ordersId;
}
