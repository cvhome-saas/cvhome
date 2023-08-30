package com.asrevo.cvhome.certificatemanager;

import com.asrevo.cvhome.certificatemanager.domain.DomainCertificateOrder;
import com.asrevo.cvhome.certificatemanager.repository.IDomainCertificateOrderRepository;
import com.asrevo.cvhome.certificatemanager.service.DomainCertificateOrderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CertificateManagerApplication {

    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(CertificateManagerApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner(DomainCertificateOrderService domainCertificateOrderService, IDomainCertificateOrderRepository iDomainCertificateOrderRepository) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                iDomainCertificateOrderRepository.deleteAll();
                DomainCertificateOrder order = new DomainCertificateOrder();
                order.setDomain("asa.asrevo.com");
                domainCertificateOrderService.order(order);
            }
        };
    }
}
