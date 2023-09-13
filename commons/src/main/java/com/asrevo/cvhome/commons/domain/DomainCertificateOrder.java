package com.asrevo.cvhome.commons.domain;

import com.asrevo.cvhome.commons.domain.challenges.Challenges;
import com.asrevo.cvhome.commons.event.order.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.asrevo.cvhome.commons.domain.CertificateOrderStatus.VALIDATION_REQUESTED;
import static org.springframework.data.relational.core.mapping.Embedded.OnEmpty.USE_NULL;


@Getter
@Setter
@Table("domain_certificate_order")
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DomainCertificateOrder extends AbstractAggregateRoot<DomainCertificateOrder> {
    private @Id Long id;
    @Embedded(onEmpty = USE_NULL)
    private OrderLocation location;
    @NotNull
    @Embedded(onEmpty = USE_NULL)
    private OrderDomain domain;
    @NotNull
    private ChallengeValidationType challengeValidationType;

    private CertificateOrderStatus certificateOrderStatus = CertificateOrderStatus.INITIATED;
    private Challenges challenges;
    private Instant createdDate;
    private Instant requestedDate;
    private Instant validatedDate;
    private Instant generatedDate;


    public static DomainCertificateOrder createOrder(OrderDomain domain, @NotNull ChallengeValidationType challengeValidationType) {
        DomainCertificateOrder order = new DomainCertificateOrder();
        order.setCertificateOrderStatus(CertificateOrderStatus.INITIATED);
        order.setDomain(domain);
        order.setChallengeValidationType(challengeValidationType);
        order.setCreatedDate(Instant.now());
        order.registerEvent(OrderCreatedEvent.from(order));
        return order;
    }

    @Override
    protected Collection<Object> domainEvents() {
        return super.domainEvents().stream().peek(it -> {
            if (it instanceof OrderEvent event) {
                if (id != null) {
                    event.setId(id);
                }
            }
        }).collect(Collectors.toList());
    }

    public void requestOrder(OrderLocation location, Challenges challenges) {
        this.setLocation(location);
        this.setCertificateOrderStatus(CertificateOrderStatus.REQUESTED);
        this.setRequestedDate(Instant.now());
        this.setChallenges(challenges);
        // @TODO check if challengeValidationType supported in order Challenges
        this.registerEvent(OrderRequestedEvent.from(this));
    }

    public void generateOrderCertificate(CertificateOrderStatus status) {
        this.setCertificateOrderStatus(status);
        if (CertificateOrderStatus.GENERATED.equals(status)) {
            this.setGeneratedDate(Instant.now());
        }
        this.registerEvent(OrderCertificateGeneratedEvent.from(this));
    }

    public void requestValidate() {
        this.certificateOrderStatus = VALIDATION_REQUESTED;
        this.registerEvent(OrderValidationRequestedEvent.from(this));
    }

    public void validated(CertificateOrderStatus certificateOrderStatus) {
        this.certificateOrderStatus = certificateOrderStatus;
        this.setValidatedDate(Instant.now());
        this.registerEvent(OrderValidatedEvent.from(this));
    }
}
