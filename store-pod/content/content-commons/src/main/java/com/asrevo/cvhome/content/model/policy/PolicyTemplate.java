package com.asrevo.cvhome.content.model.policy;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import com.asrevo.cvhome.content.model.PolicyType;
import com.asrevo.cvhome.content.model.common.ContentTranslation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Starter text for a policy type and jurisdiction, per locale.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PolicyTemplate implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private PolicyType type;

    private String jurisdiction;

    private List<ContentTranslation> translations;

}
