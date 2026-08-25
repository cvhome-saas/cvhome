package com.asrevo.cvhome.content.model.common;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

/**
 * Body of {@code POST …/publish}. A future {@code publishAt} schedules instead of publishing now.
 */
@Getter
@Setter
public class PublishRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Instant publishAt;

    private Instant unpublishAt;

}
