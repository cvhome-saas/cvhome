package com.asrevo.cvhome.store.core.model.security;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

/**
 * Object used for reading a group
 *
 * @author carlsamson
 */
@Setter
@Getter
public class ReadableGroup extends GroupEntity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private Long id = 0L;
}
