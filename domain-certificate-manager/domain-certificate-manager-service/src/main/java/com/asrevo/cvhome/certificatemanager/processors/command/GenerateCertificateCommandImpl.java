package com.asrevo.cvhome.certificatemanager.processors.command;

import com.asrevo.cvhome.certificatemanager.commons.command.order.GenerateCertificateCommand;
import com.asrevo.cvhome.certificatemanager.service.AcmCertificateOrderService;
import com.asrevo.cvhome.commons.command.CommandImpl;
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