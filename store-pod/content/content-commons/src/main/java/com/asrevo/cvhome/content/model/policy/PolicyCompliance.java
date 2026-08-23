package com.asrevo.cvhome.content.model.policy;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.PolicyType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One row of {@code GET /policies/compliance}: a policy type, who requires it, and whether the store has it.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PolicyCompliance implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private PolicyType type;

    private List<String> requiredBy;

    /**
     * The head's status, or {@code null} when the store has no policy of that type.
     */
    private ContentStatus status;

    private Long id;

}
