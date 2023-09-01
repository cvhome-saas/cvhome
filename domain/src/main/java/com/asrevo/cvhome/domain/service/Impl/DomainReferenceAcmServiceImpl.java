package com.asrevo.cvhome.domain.service.Impl;

import com.asrevo.cvhome.commons.domain.DomainCertificateOrder;
import com.asrevo.cvhome.commons.domain.DomainReferenceOrder;
import com.asrevo.cvhome.commons.domain.DomainReference;
import com.asrevo.cvhome.commons.domain.DomainType;
import com.asrevo.cvhome.domain.service.AcmService;
import com.asrevo.cvhome.domain.service.DomainReferenceAcmService;
import com.asrevo.cvhome.domain.service.DomainReferenceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
@Slf4j
public class DomainReferenceAcmServiceImpl implements DomainReferenceAcmService {
    private final AcmService acmService;
    private final DomainReferenceService domainReferenceService;

    @Override
    public Mono<DomainReferenceOrder> order(Long domainId) {
        return domainReferenceService.findById(domainId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new Exception("cant find this domainId : " + domainId))))
                .flatMap(domainReference -> {
                            DomainCertificateOrder order = new DomainCertificateOrder();
                            order.setDomain(domainReference.domain());
                            return acmService.order(order).map(response -> response);
                        }
                )
                .flatMap(it -> {
                    if (it.getStatusCode().isSameCodeAs(HttpStatus.OK) && it.getBody() != null) {
                        return Mono.just(it.getBody());
                    } else {
                        return Mono.error(() -> new Exception("error when ordering domainId : " + domainId + " from acm with status code " + it.getStatusCode()));
                    }
                })
                .flatMap(it ->
                        domainReferenceService.updateExternalAcmOrderId(domainId, it.getId())
                                .map(newDomainReference -> new DomainReferenceOrder(newDomainReference, it))
                );
    }

    @Override
    public Flux<DomainReferenceOrder> getAllDomainReferences(String sub, DomainType domainType) {

        Mono<List<DomainReference>> domainReferencesList = domainReferenceService.getAllDomainReferences(sub, domainType).collectList();
        return domainReferencesList.flatMap(domainReferences -> {

            Map<DomainType, List<DomainReference>> domainGroups = domainReferences
                    .stream()
                    .collect(Collectors.groupingBy(DomainReference::domainType));


            List<Long> customDomainExternalAcmOrderIds = Optional.ofNullable(domainGroups.get(DomainType.CUSTOM))
                    .orElse(List.of())
                    .stream()
                    .map(DomainReference::externalAcmOrderId)
                    .filter(Objects::nonNull)
                    .toList();

            return acmService.findOrders(customDomainExternalAcmOrderIds).map(response -> {
                Map<Long, DomainCertificateOrder> orders;

                if (response.getStatusCode().isSameCodeAs(HttpStatus.OK) && response.getBody() != null) {
                    orders = response.getBody().stream()
                            .collect(Collectors.toMap(DomainCertificateOrder::getId, Function.identity()));
                } else {
                    orders = Map.of();
                }

                return domainReferences
                        .stream()
                        .map(domainReference -> {
                            DomainCertificateOrder domainCertificateOrder;
                            if (domainReference.externalAcmOrderId() != null) {
                                domainCertificateOrder = orders.get(domainReference.externalAcmOrderId());
                            } else {
                                domainCertificateOrder = null;
                            }
                            return new DomainReferenceOrder(domainReference, domainCertificateOrder);
                        })
                        .toList();
            });
        }).flatMapMany(Flux::fromIterable);
    }
}