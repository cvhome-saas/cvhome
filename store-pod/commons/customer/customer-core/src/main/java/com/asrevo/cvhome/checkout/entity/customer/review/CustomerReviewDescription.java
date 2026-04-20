/* (C)2006-2010 */
package com.asrevo.cvhome.checkout.entity.customer.review;

import java.io.Serial;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.Description;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CUSTOMER_REVIEW_DESCRIPTION",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"CUSTOMER_REVIEW_ID", "LANGUAGE_CODE"})})
@TableGenerator(name = "description_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "CUSTOMER_REVIEW_DESCRIPTION_SEQ_NEXT_VAL",
        allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
        initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
@Getter
@Setter
public class CustomerReviewDescription extends Description {

    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne(targetEntity = CustomerReview.class)
    @JoinColumn(name = "CUSTOMER_REVIEW_ID")
    private CustomerReview customerReview;

}
