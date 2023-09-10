package com.asrevo.cvhome.certificatemanager.jobs;

import com.asrevo.cvhome.certificatemanager.service.DomainCertificateOrderService;
import com.asrevo.cvhome.commons.domain.DomainCertificateOrder;
import lombok.AllArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class AcmJobs {
    private final DomainCertificateOrderService domainCertificateOrderService;
    private final StreamBridge streamBridge;

    public void run() {
        List<DomainCertificateOrder> orders = domainCertificateOrderService.findAllRequestedCertificates(100);
        orders.forEach(order -> {
            System.out.println("order");
            streamBridge.send("logOrderEvents-out-0", order);
        });

    }
}
