package com.asrevo.cvhome.domaincertificatemanager.commons.command.order;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.OrdersId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestOrderCertificateCommand extends OrderCommand {
    private OrdersId ordersId;
}
