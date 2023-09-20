package com.asrevo.cvhome.commons.command.order;

import com.asrevo.cvhome.commons.domain.CertificateId;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.OrdersId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCertificateToDomainCommand extends OrderCommand {
    private Domain domain;
    private CertificateId certificateId;
    private OrdersId ordersId;

}
