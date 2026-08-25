package com.asrevo.cvhome.content.model.policy;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

/**
 * Body of {@code POST /policies/{id}/publish-version}: cuts a new immutable version from the head's current
 * text and makes it live.
 */
@Getter
@Setter
public class PublishPolicyVersionRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Instant effectiveFrom;

    @Size(max = 200)
    private String note;

}
