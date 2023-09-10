package com.asrevo.cvhome.certificatemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CertificateManagerApplication {

    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(CertificateManagerApplication.class, args);
    }

/*    @Bean
    public CommandLineRunner runner(DomainCertificateOrderRepository domainCertificateOrderRepository, AcmCertificateOrderService acmCertificateOrderService) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                domainCertificateOrderRepository.deleteAll();
                DomainCertificateOrder order = new DomainCertificateOrder();
                order.setDomain("asas.asrevo.com");
                order.setChallengeValidationType(ChallengeValidationType.TlsAlpn01);
                acmCertificateOrderService.initiateOrder(order);
            }
        };
    }*/
}
