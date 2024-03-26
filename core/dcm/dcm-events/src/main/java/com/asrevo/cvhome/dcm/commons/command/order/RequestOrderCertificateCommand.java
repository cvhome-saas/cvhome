package com.asrevo.cvhome.dcm.commons.command.order;

import com.asrevo.cvhome.dcm.commons.domain.OrdersId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestOrderCertificateCommand extends OrderCommand {
    private OrdersId ordersId;
}
