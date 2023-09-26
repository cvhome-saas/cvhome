package com.asrevo.cvhome.certificatemanager.processors.command;

import com.asrevo.cvhome.certificatemanager.service.AcmCertificateOrderService;
import com.asrevo.cvhome.commons.command.CommandImpl;
import com.asrevo.cvhome.commons.command.order.CreateOrderCommand;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class CreateOrderCommandImpl implements CommandImpl<CreateOrderCommand> {
    private final AcmCertificateOrderService acmCertificateOrderService;

    @Override
    public void process(CreateOrderCommand order) {
        acmCertificateOrderService.initiateOrder(order.getDomain(), order.getDomain().getRecommendedChallengeValidationType());
    }

    @Override
    public String type() {
        return CreateOrderCommand.class.getName();
    }
}
