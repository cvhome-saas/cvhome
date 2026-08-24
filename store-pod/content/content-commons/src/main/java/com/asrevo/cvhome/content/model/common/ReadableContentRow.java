package com.asrevo.cvhome.content.model.common;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import lombok.Getter;
import lombok.Setter;

/**
 * One row of a console list — exactly what the table renders, no more.
 */
@Getter
@Setter
public class ReadableContentRow implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private ContentType type;

    private String slug;

    private String title;

    /**
     * Type-specific second line: placement · target for banners, group · position for FAQ, policy type · version for
     * policies, the slug for everything else.
     */
    private String subtitle;

    private ContentStatus status;

    private Instant publishAt;

    private List<LocaleState> locales;

    private Instant updatedAt;

    private String updatedBy;

}
