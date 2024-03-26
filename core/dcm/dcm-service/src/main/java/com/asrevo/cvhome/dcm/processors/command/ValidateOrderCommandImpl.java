package com.asrevo.cvhome.dcm.processors.command;

import com.asrevo.cvhome.commons.command.CommandImpl;
import com.asrevo.cvhome.dcm.commons.command.order.ValidateOrderCommand;
import com.asrevo.cvhome.dcm.service.AcmCertificateOrderService;
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
