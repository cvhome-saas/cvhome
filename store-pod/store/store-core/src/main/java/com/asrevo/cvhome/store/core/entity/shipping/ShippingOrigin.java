package com.asrevo.cvhome.store.core.entity.shipping;

import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.entity.reference.zone.Zone;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "SHIPING_ORIGIN")
@Getter
@Setter
public class ShippingOrigin extends SalesManagerEntity<Long, ShippingOrigin> {


    /**
     *
     */
    private static final long serialVersionUID = 1172536723717691214L;


    @Id
    @Column(name = "SHIP_ORIGIN_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT",
            pkColumnValue = "SHP_ORIG_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Column(name = "ACTIVE")
    private boolean active;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_ID", nullable = false)
    private MerchantStore merchantStore;

    @NotEmpty
    @Column(name = "STREET_ADDRESS", length = 256)
    private String address;


    @NotEmpty
    @Column(name = "CITY", length = 100)
    private String city;

    @NotEmpty
    @Column(name = "POSTCODE", length = 20)
    private String postalCode;

    @Column(name = "STATE", length = 100)
    private String state;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = Country.class)
    @JoinColumn(name = "COUNTRY_ID", nullable = true)
    private Country country;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = Zone.class)
    @JoinColumn(name = "ZONE_ID", nullable = true)
    private Zone zone;

}
