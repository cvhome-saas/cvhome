package com.asrevo.cvhome.dcm.processors.command;

import com.asrevo.cvhome.commons.command.CommandImpl;
import com.asrevo.cvhome.dcm.commons.command.order.RequestOrderCertificateCommand;
import com.asrevo.cvhome.dcm.service.AcmCertificateOrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class OrderCertificateCommandImpl implements CommandImpl<RequestOrderCertificateCommand> {
    private final AcmCertificateOrderService acmCertificateOrderService;

    @Override
    public void process(RequestOrderCertificateCommand order) {
        acmCertificateOrderService.order(order.getId());
    }

    @Override
    public String type() {
        return RequestOrderCertificateCommand.class.getName();
    }
}
