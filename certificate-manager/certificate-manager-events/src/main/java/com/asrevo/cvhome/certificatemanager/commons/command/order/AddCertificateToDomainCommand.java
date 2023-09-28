package com.asrevo.cvhome.certificatemanager.commons.command.order;

import com.asrevo.cvhome.certificatemanager.commons.domain.CertificateId;
import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.certificatemanager.commons.domain.OrdersId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCertificateToDomainCommand extends OrderCommand {
    private Domain domain;
    private CertificateId certificateId;
    private OrdersId ordersId;

}
