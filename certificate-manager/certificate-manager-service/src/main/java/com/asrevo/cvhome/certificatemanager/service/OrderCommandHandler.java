package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.commons.command.Command;
import com.asrevo.cvhome.commons.command.order.ValidateOrderCommand;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class OrderCommandHandler {
    private final AcmCertificateOrderService acmCertificateOrderService;

    public void handle(Command command) {

        if (command instanceof ValidateOrderCommand order) {
            acmCertificateOrderService.doValidation(order.getId());
        }
    }

}
