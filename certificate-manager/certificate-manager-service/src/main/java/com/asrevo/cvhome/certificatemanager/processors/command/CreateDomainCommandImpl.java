package com.asrevo.cvhome.certificatemanager.processors.command;

import com.asrevo.cvhome.certificatemanager.commons.command.domain.CreateDomainCommand;
import com.asrevo.cvhome.certificatemanager.service.AcmCertificateOrderService;
import com.asrevo.cvhome.commons.command.CommandImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class CreateDomainCommandImpl implements CommandImpl<CreateDomainCommand> {
    private final AcmCertificateOrderService acmCertificateOrderService;

    @Override
    public void process(CreateDomainCommand command) {
        acmCertificateOrderService.register(command.getDomain(), command.isAutoRenew(), command.isAutoOrder());
    }

    @Override
    public String type() {
        return CreateDomainCommand.class.getName();
    }
}
