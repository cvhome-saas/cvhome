package com.asrevo.cvhome.content.model.page;

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
public class ReadablePage extends PersistablePage {

    @Serial
    private static final long serialVersionUID = 1L;

    private ContentType type = ContentType.PAGE;

    private ContentStatus status;

    private List<LocaleState> locales;

    private ContentAudit audit;

}
