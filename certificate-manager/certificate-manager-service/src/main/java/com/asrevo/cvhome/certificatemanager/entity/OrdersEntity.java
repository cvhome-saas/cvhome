package com.asrevo.cvhome.certificatemanager.entity;

import com.asrevo.cvhome.certificatemanager.domain.challenges.Challenges;
import com.asrevo.cvhome.commons.domain.*;
import com.asrevo.cvhome.commons.event.order.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.asrevo.cvhome.commons.domain.CertificateOrderStatus.VALIDATION_REQUESTED;
import static org.springframework.data.relational.core.mapping.Embedded.OnEmpty.USE_NULL;

@Getter
@Setter
@Table("orders")
public class OrdersEntity extends BaseEntity<OrdersEntity, OrdersId> {
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
    @MappedCollection(idColumn = "orders_id")
    private CertificateEntity certificate;

    public static OrdersEntity createOrder(OrderDomain domain, @NotNull ChallengeValidationType challengeValidationType) {
        OrdersEntity order = new OrdersEntity();
        order.setNew();
        order.setCertificateOrderStatus(CertificateOrderStatus.INITIATED);
        order.setDomain(domain);
        order.setChallengeValidationType(challengeValidationType);
        order.setCreatedDate(Instant.now());
        order.registerEvent(OrderCreatedEvent.from(order.createdDate));
        return order;
    }

    @Override
    protected OrdersId generateId() {
        return OrdersId.newId();
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
        this.registerEvent(OrderRequestedEvent.from(this.location, this.requestedDate));
    }

    public void generateOrderCertificate(CertificateOrderStatus status) {
        this.setCertificateOrderStatus(status);
        if (CertificateOrderStatus.GENERATED.equals(status)) {
            this.setGeneratedDate(Instant.now());
        }
        this.registerEvent(OrderCertificateGeneratedEvent.from(this.certificateOrderStatus, this.generatedDate));
    }

    public void requestValidate() {
        this.certificateOrderStatus = VALIDATION_REQUESTED;
        this.registerEvent(OrderValidationRequestedEvent.from());
    }

    public void validated(CertificateOrderStatus certificateOrderStatus) {
        this.certificateOrderStatus = certificateOrderStatus;
        this.setValidatedDate(Instant.now());
        this.registerEvent(OrderValidatedEvent.from(this.validatedDate, this.certificateOrderStatus));
    }
}
