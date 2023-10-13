package com.asrevo.cvhome.domaincertificatemanager.processors.command;

import com.asrevo.cvhome.domaincertificatemanager.commons.command.order.ValidateOrderCommand;
import com.asrevo.cvhome.domaincertificatemanager.service.AcmCertificateOrderService;
import com.asrevo.cvhome.commons.command.CommandImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class ValidateOrderCommandImpl implements CommandImpl<ValidateOrderCommand> {
    private final AcmCertificateOrderService acmCertificateOrderService;

    @Override
    public void process(ValidateOrderCommand command) {
        acmCertificateOrderService.validate(command.getId());
    }

    @Override
    public String type() {
        return ValidateOrderCommand.class.getName();
    }
}
