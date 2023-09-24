package com.asrevo.cvhome.certificatemanager.entity;

import com.asrevo.cvhome.certificatemanager.domain.DomainCertificate;
import com.asrevo.cvhome.certificatemanager.domain.challenges.Challenge;
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
import java.util.Optional;

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
    private Domain domain;
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

    public static OrdersEntity createOrder(Domain domain, @NotNull ChallengeValidationType challengeValidationType) {
        OrdersEntity order = new OrdersEntity();
        order.setNew();
        order.setCertificateOrderStatus(CertificateOrderStatus.INITIATED);
        order.setDomain(domain);
        order.setChallengeValidationType(challengeValidationType);
        order.setCreatedDate(Instant.now());
        order.registerEvent(OrdersCreatedEvent.from(order.createdDate));
        return order;
    }

    @Override
    protected OrdersId generateId() {
        return OrdersId.newId();
    }


    public void requestOrder(OrderLocation location, Challenges challenges) {
        this.setLocation(location);
        this.setCertificateOrderStatus(CertificateOrderStatus.REQUESTED);
        this.setRequestedDate(Instant.now());
        this.setChallenges(challenges);
        if (challenges.getChallenge(this.challengeValidationType) == null) {
            ChallengeValidationType oldType = this.challengeValidationType;
            this.challengeValidationType = challenges.challenges().stream().findFirst().map(Challenge::type).orElse(null);
            if (challengeValidationType == null) {
                throw new RuntimeException("challenges don't have any valid validation type");
            }
            this.registerEvent(OrdersChallengeValidationTypeChangedEvent.from(domain, oldType, this.challengeValidationType));
        }
        this.registerEvent(OrdersRequestedEvent.from(this.domain, this.location, this.requestedDate));
        // @TODO check if challengeValidationType supported in order Challenges

    }

    public void generateOrderCertificate(DomainCertificate certificate) {

        CertificateOrderStatus orderStatus = Optional.ofNullable(certificate).map(d -> CertificateOrderStatus.GENERATED).orElse(CertificateOrderStatus.FAIL_GENERATING);
        this.setCertificateOrderStatus(orderStatus);
        if (CertificateOrderStatus.GENERATED.equals(orderStatus)) {
            this.setGeneratedDate(Instant.now());
            this.certificate = CertificateEntity.createNewCertificate(certificate.getNotAfter().toInstant(), certificate.getNotBefore().toInstant(), certificate.getSerialNumber(), certificate.getVersion(), certificate.getSigAlgName(), certificate.getSigAlgOID());
        }
        this.andEvent(this.certificate);
        this.registerEvent(OrdersCertificateGeneratedEvent.from(this.certificateOrderStatus, this.generatedDate));
    }

    public void requestValidate() {
        this.certificateOrderStatus = VALIDATION_REQUESTED;
        this.registerEvent(OrdersValidationRequestedEvent.from());
    }

    public void validated(CertificateOrderStatus certificateOrderStatus) {
        this.certificateOrderStatus = certificateOrderStatus;
        this.setValidatedDate(Instant.now());
        this.registerEvent(OrdersValidatedEvent.from(this.validatedDate, this.certificateOrderStatus));
    }
}
