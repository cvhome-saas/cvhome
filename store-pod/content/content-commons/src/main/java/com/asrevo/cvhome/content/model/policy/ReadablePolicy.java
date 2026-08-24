package com.asrevo.cvhome.content.model.policy;

import java.io.Serial;
import java.util.List;

import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.common.ContentAudit;
import com.asrevo.cvhome.content.model.common.LocaleState;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadablePolicy extends PersistablePolicy {

    @Serial
    private static final long serialVersionUID = 1L;

    private ContentType type = ContentType.POLICY;

    private ContentStatus status;

    private List<LocaleState> locales;

    private ContentAudit audit;

    /**
     * The live version number, or 0 when nothing has been published yet.
     */
    private int liveVersion;

    private List<ReadablePolicyVersion> versions;

}
