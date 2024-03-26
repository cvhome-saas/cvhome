package com.asrevo.cvhome.dcm.processors.command;

import com.asrevo.cvhome.commons.command.CommandImpl;
import com.asrevo.cvhome.dcm.commons.command.order.CreateOrderCommand;
import com.asrevo.cvhome.dcm.service.AcmCertificateOrderService;
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
        acmCertificateOrderService.initiate(order.getDomain(), order.getValidationType(), order.isIncludeSubDomains());
    }

    @Override
    public String type() {
        return CreateOrderCommand.class.getName();
    }
}
