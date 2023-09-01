package com.asrevo.cvhome.commons.domain;

import com.asrevo.cvhome.commons.event.Event;
import com.asrevo.cvhome.commons.event.SimpleEvent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.asrevo.cvhome.commons.utils.Defines.OrderEvents.*;


@Getter
@Setter
@Table("domain_certificate_order")
@NoArgsConstructor
@AllArgsConstructor
public class DomainCertificateOrder extends AbstractAggregateRoot<DomainCertificateOrder> {
    private @Id Long id;
    private String location;
    @NotNull
    private String domain;

    private CertificateOrderStatus certificateOrderStatus;
    private Challenges challenges;


    @Override
    protected Collection<Object> domainEvents() {
        return super.domainEvents().stream().peek(it -> {
            if (it instanceof Event event) {
                if (id != null) {
                    event.data().put(DOMAIN_CERTIFICATE_ORDER_ID_KEY, id.toString());
                }
            }
        }).collect(Collectors.toList());
    }

    public static DomainCertificateOrder newOrder() {
        DomainCertificateOrder domainCertificateOrder = new DomainCertificateOrder();
        domainCertificateOrder.registerEvent(new SimpleEvent(DOMAIN_CERTIFICATE_ORDER_CREATED, new HashMap<>()));
        return domainCertificateOrder;
    }

    public void setCertificateOrderStatus(CertificateOrderStatus status) {
        this.certificateOrderStatus = status;
        if (status != null) {
            Map<String, String> data = Map.of(DOMAIN_CERTIFICATE_ORDER_STATUS_CHANGED_KEY, status.name());
            this.registerEvent(new SimpleEvent(DOMAIN_CERTIFICATE_ORDER_STATUS_CHANGED_EVENT, new HashMap<>(data)));
        }
    }
}
