package com.asrevo.cvhome.store.core.entity.payments;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
public enum PaymentType {

    COD(),

    MANUAL_TRANSFER(),

    STRIPE(List.of(
            requiredString("clientId"),
            requiredString("secretKey")
    )),

    PAYPAL(List.of(
            requiredString("clientId"),
            requiredString("secretKey")
    ));

    private final List<Attr> attrs;

    PaymentType() {
        this.attrs = List.of();
    }

    PaymentType(List<Attr> attrs) {
        this.attrs = List.copyOf(attrs);
    }

    private static Attr requiredString(String name) {
        return Attr.builder()
                .name(name)
                .type(AttrType.STRING)
                .required(true)
                .build();
    }

    public enum AttrType {
        INTEGER,
        STRING,
        DATE
    }

    @Builder
    public record Attr(
            String name,
            AttrType type,
            String defaultValue,
            boolean required
    ) {
    }
}