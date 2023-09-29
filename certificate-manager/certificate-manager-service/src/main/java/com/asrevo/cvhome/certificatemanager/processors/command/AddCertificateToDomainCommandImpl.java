package com.asrevo.cvhome.certificatemanager.processors.command;

import com.asrevo.cvhome.certificatemanager.commons.command.order.AddCertificateToDomainCommand;
import com.asrevo.cvhome.certificatemanager.service.DomainService;
import com.asrevo.cvhome.commons.command.CommandImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class AddCertificateToDomainCommandImpl implements CommandImpl<AddCertificateToDomainCommand> {
    private final DomainService domainService;

    @Override
    public void process(AddCertificateToDomainCommand certificate) {
        domainService.addNewCertificateToDomain(certificate.getDomain(), certificate.getOrdersId(), certificate.getCertificateId());
    }

    @Override
    public String type() {
        return AddCertificateToDomainCommand.class.getName();
    }
}
