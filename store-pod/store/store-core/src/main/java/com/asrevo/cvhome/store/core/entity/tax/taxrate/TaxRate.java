/*
 * Licensed to csti consulting
 * You may obtain a copy of the License at
 *
 * http://www.csticonsulting.com
 * Copyright (c) 2006-Aug 24, 2010 Consultation CS-TI inc.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.asrevo.cvhome.store.core.entity.tax.taxrate;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.entity.reference.zone.Zone;
import com.asrevo.cvhome.store.core.entity.tax.taxclass.TaxClass;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "TAX_RATE", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "TAX_CODE",
                "MERCHANT_ID"
        })
}
)
@Getter
@Setter
public class TaxRate extends SalesManagerEntity<Long, TaxRate> implements Auditable {
    @Serial
    private static final long serialVersionUID = 3356827741612925066L;

    @Id
    @Column(name = "TAX_RATE_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "TAX_RATE_ID_NEXT_VALUE", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Column(name = "TAX_PRIORITY")
    private Integer taxPriority = 0;

    @Column(name = "TAX_RATE", nullable = false, precision = 7, scale = 4)
    private BigDecimal taxRate;

    @NotEmpty
    @Column(name = "TAX_CODE")
    private String code;


    @Column(name = "PIGGYBACK")
    private boolean piggyback;

    @ManyToOne
    @JoinColumn(name = "TAX_CLASS_ID", nullable = false)
    private TaxClass taxClass;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_ID", nullable = false)
    private MerchantStore merchantStore;

    @Valid
    @OneToMany(mappedBy = "taxRate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaxRateDescription> descriptions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Country.class)
    @JoinColumn(name = "COUNTRY_ID", nullable = false)
    private Country country;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ZONE_ID")
    private Zone zone;

    @Column(name = "STORE_STATE_PROV", length = 100)
    private String stateProvince;

    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    private TaxRate parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<TaxRate> taxRates = new ArrayList<>();

    @Transient
    private String rateText;


    public TaxRate() {
    }
}