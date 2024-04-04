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
import com.asrevo.cvhome.store.core.entity.common.description.Description;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;


@Entity
@Table(name = "TAX_RATE_DESCRIPTION", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "TAX_RATE_ID",
                "LANGUAGE_ID"
        })
}
)

@TableGenerator(name = "description_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "taxrate_description_seq", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
//@SequenceGenerator(name = "description_gen", sequenceName = "taxrate_description_seq", allocationSize = SchemaConstant.DESCRIPTION_ID_SEQUENCE_START)
@Getter
@Setter
public class TaxRateDescription extends Description {
    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne(targetEntity = TaxRate.class)
    @JoinColumn(name = "TAX_RATE_ID")
    private TaxRate taxRate;

    public TaxRateDescription() {
    }

}
