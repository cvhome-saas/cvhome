package com.asrevo.cvhome.content.model.policy;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import com.asrevo.cvhome.content.model.PolicyVersionStatus;
import com.asrevo.cvhome.content.model.common.ContentTranslation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadablePolicyVersion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer version;

    private PolicyVersionStatus status;

    private String note;

    private Instant effectiveFrom;

    private Instant publishedAt;

    private String publishedBy;

    /**
     * Heading and body per locale; only on the single-version read.
     */
    private List<ContentTranslation> translations;

}
