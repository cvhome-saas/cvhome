package com.asrevo.cvhome.content.entity.policy;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.content.Content;
import com.asrevo.cvhome.content.model.policy.PolicyDisplayLocation;
import com.asrevo.cvhome.content.model.policy.PolicyType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONTENT_POLICY")
@Getter
@Setter
public class ContentPolicy {
    @Id
    @Column(name = "CONTENT_ID")
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "CONTENT_ID")
    private Content content;
    @Embedded
    @AttributeOverride(name = "storeMerchantId", column = @Column(name = "STORE_MERCHANT_ID", length = 50))
    private StoreMerchantId storeMerchantId;
    @Enumerated(EnumType.STRING)
    @Column(name = "POLICY_TYPE", nullable = false, length = 30)
    private PolicyType policyType;
    @Column(name = "POLICY_VERSION", nullable = false, length = 50)
    private String policyVersion;
    @Column(name = "EFFECTIVE_DATE", nullable = false)
    private LocalDate effectiveDate;
    @Column(name = "ACCEPTANCE_REQUIRED", nullable = false)
    private boolean acceptanceRequired;
    @Column(name = "JURISDICTION", length = 100)
    private String jurisdiction;
    @Column(name = "ACTIVE", nullable = false)
    private boolean active;
    @ElementCollection
    @CollectionTable(name = "POLICY_DISPLAY_LOCATION", joinColumns = @JoinColumn(name = "POLICY_CONTENT_ID"))
    @Enumerated(EnumType.STRING)
    @Column(name = "DISPLAY_LOCATION", nullable = false, length = 30)
    private Set<PolicyDisplayLocation> displayLocations = new HashSet<>();
}
