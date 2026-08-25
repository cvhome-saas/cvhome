package com.asrevo.cvhome.content.model.policy;

import java.io.Serial;
import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.asrevo.cvhome.content.model.PolicyType;
import com.asrevo.cvhome.content.model.common.PersistableContent;

import lombok.Getter;
import lombok.Setter;

/**
 * A legal document. The head row carries the type and the draft text (heading = title, body); publishing cuts an
 * immutable {@code PolicyVersion} from it.
 */
@Getter
@Setter
public class PersistablePolicy extends PersistableContent {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private PolicyType policyType;

    @Size(max = 60)
    private String jurisdiction;

    private Instant effectiveFrom;

    private boolean requiresAcceptance;

    private boolean notifyCustomers;

    private boolean showInFooter = true;

    private boolean showAtCheckout;

    private boolean showAtSignup;

}
