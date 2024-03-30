package com.asrevo.cvhome.store.core.entity.order;

import com.asrevo.cvhome.store.core.converter.LocaleConverter;
import com.asrevo.cvhome.store.core.entity.common.Billing;
import com.asrevo.cvhome.store.core.entity.common.Delivery;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.attributes.OrderAttribute;
import com.asrevo.cvhome.store.core.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatusHistory;
import com.asrevo.cvhome.store.core.entity.order.payment.CreditCard;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.store.core.entity.reference.currency.Currency;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLOrder;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "ORDERS")
@Getter
@Setter
public class Order extends SalesManagerEntity<Long, Order> {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ORDER_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT",
            pkColumnValue = "ORDER_ID_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Column(name = "ORDER_STATUS")
    @Enumerated(value = EnumType.STRING)
    private OrderStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_MODIFIED")
    private Date lastModified;

    //the customer object can be detached. An order can exist and the customer deleted
    @Column(name = "CUSTOMER_ID")
    private Long customerId;

    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_PURCHASED")
    private Date datePurchased;

    //used for an order payable on multiple installment
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ORDER_DATE_FINISHED")
    private Date orderDateFinished;

    //What was the exchange rate
    @Column(name = "CURRENCY_VALUE")
    private BigDecimal currencyValue = new BigDecimal(1);//default 1-1

    @Column(name = "ORDER_TOTAL")
    private BigDecimal total;

    @Column(name = "IP_ADDRESS")
    private String ipAddress;

    @Column(name = "CART_CODE", nullable = true)
    private String shoppingCartCode;

    @Column(name = "CHANNEL")
    @Enumerated(value = EnumType.STRING)
    private OrderChannel channel;

    @Column(name = "ORDER_TYPE")
    @Enumerated(value = EnumType.STRING)
    private OrderType orderType = OrderType.ORDER;

    @Column(name = "PAYMENT_TYPE")
    @Enumerated(value = EnumType.STRING)
    private PaymentType paymentType;

    @Column(name = "PAYMENT_MODULE_CODE")
    private String paymentModuleCode;

    @Column(name = "SHIPPING_MODULE_CODE")
    private String shippingModuleCode;

    @Column(name = "CUSTOMER_AGREED")
    private Boolean customerAgreement = false;

    @Column(name = "CONFIRMED_ADDRESS")
    private Boolean confirmedAddress = false;

    @Embedded
    private Delivery delivery = null;

    @Valid
    @Embedded
    private Billing billing = null;

    @Embedded
    @Deprecated
    private CreditCard creditCard = null;


    @ManyToOne(targetEntity = Currency.class)
    @JoinColumn(name = "CURRENCY_ID")
    private Currency currency;

    @Column(name = "LOCALE")
    @Convert(converter = LocaleConverter.class)
    private Locale locale;


    @JsonIgnore
    @ManyToOne(targetEntity = MerchantStore.class)
    @JoinColumn(name = "MERCHANTID")
    private MerchantStore merchant;

    //@OneToMany(mappedBy = "order")
    //private Set<OrderAccount> orderAccounts = new HashSet<OrderAccount>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private Set<OrderProduct> orderProducts = new LinkedHashSet<OrderProduct>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @SQLOrder(value = "sort_order asc")
    private Set<OrderTotal> orderTotal = new LinkedHashSet<OrderTotal>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @SQLOrder(value = "ORDER_STATUS_HISTORY_ID asc")
    private Set<OrderStatusHistory> orderHistory = new LinkedHashSet<OrderStatusHistory>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private Set<OrderAttribute> orderAttributes = new LinkedHashSet<OrderAttribute>();
    @Column(name = "CUSTOMER_EMAIL_ADDRESS", length = 50, nullable = false)
    private String customerEmailAddress;

    public Order() {
    }


}