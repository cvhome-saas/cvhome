package com.asrevo.cvhome.dcm.processors.command;

import com.asrevo.cvhome.commons.command.CommandImpl;
import com.asrevo.cvhome.dcm.commons.command.order.GenerateCertificateCommand;
import com.asrevo.cvhome.dcm.service.AcmCertificateOrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class GenerateCertificateCommandImpl implements CommandImpl<GenerateCertificateCommand> {
    private final AcmCertificateOrderService acmCertificateOrderService;

    @Override
    public void process(GenerateCertificateCommand order) {
        acmCertificateOrderService.doGeneration(order.getId());
    }

    @Override
    public String type() {
        return GenerateCertificateCommand.class.getName();
    }
}