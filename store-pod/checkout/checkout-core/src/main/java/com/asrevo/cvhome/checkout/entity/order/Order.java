package com.asrevo.cvhome.checkout.entity.order;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.validation.Valid;

import org.hibernate.annotations.SQLOrder;

import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.checkout.entity.order.orderstatus.OrderStatusHistory;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.converter.CurrencyCodeConverter;
import com.asrevo.cvhome.store.core.converter.LocaleConverter;
import com.asrevo.cvhome.store.core.entity.common.Billing;
import com.asrevo.cvhome.store.core.entity.common.Delivery;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.model.reference.CurrencyCode;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ORDERS")
@Getter
@Setter
public class Order extends SalesManagerEntity<Long, Order> {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ORDER_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "ORDER_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Column(name = "ORDER_STATUS")
    @Enumerated(value = EnumType.STRING)
    private OrderStatus status;

    @Column(name = "LAST_MODIFIED")
    private Instant lastModified;

    // the customer object can be detached. An order can exist and the customer deleted
    @Column(name = "CUSTOMER_ID")
    private Long customerId;

    @Column(name = "DATE_PURCHASED")
    private LocalDate datePurchased;

    // used for an order payable on multiple installment
    @Column(name = "ORDER_DATE_FINISHED")
    private Instant orderDateFinished;

    // What was the exchange rate
    @Column(name = "CURRENCY_VALUE")
    private BigDecimal currencyValue = new BigDecimal(1); // default 1-1

    @Column(name = "ORDER_TOTAL")
    private BigDecimal total;

    @Column(name = "IP_ADDRESS")
    private String ipAddress;

    @Column(name = "CART_CODE")
    private String shoppingCartCode;

    @Column(name = "CHANNEL")
    @Enumerated(value = EnumType.STRING)
    private OrderChannel channel;

    @Column(name = "ORDER_TYPE")
    @Enumerated(value = EnumType.STRING)
    private OrderType orderType = OrderType.ORDER;

    @Column(name = "CUSTOMER_AGREED")
    private Boolean customerAgreement = false;

    @Column(name = "CONFIRMED_ADDRESS")
    private Boolean confirmedAddress = false;

    @Embedded
    private Delivery delivery;

    @Valid
    @Embedded
    private Billing billing;

    @JsonIgnore
    @Column(name = "CURRENCY_ID", length = 6, nullable = false)
    @Convert(converter = CurrencyCodeConverter.class)
    private CurrencyCode currency;

    @Column(name = "LOCALE")
    @Convert(converter = LocaleConverter.class)
    private Locale locale;

    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50))
    private StoreMerchantId storeMerchantId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private Set<OrderProduct> orderProducts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @SQLOrder(value = "sort_order asc")
    private Set<OrderTotal> orderTotal = new LinkedHashSet<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @SQLOrder(value = "ORDER_STATUS_HISTORY_ID asc")
    private Set<OrderStatusHistory> orderHistory = new LinkedHashSet<>();

    @Column(name = "CUSTOMER_EMAIL_ADDRESS", length = 50, nullable = false)
    private String customerEmailAddress;
}