package com.asrevo.cvhome.content.model.section;

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
public class ReadableSection extends PersistableSection {

    @Serial
    private static final long serialVersionUID = 1L;

    private ContentType type = ContentType.SECTION;

    private ContentStatus status;

    private List<LocaleState> locales;

    private ContentAudit audit;

    /** The resolved URL of {@code mediaId}, so the console can show a thumbnail without a second call. */
    private String imageUrl;

}
