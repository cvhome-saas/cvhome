package com.asrevo.cvhome.content.model.section;

import java.io.Serial;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.asrevo.cvhome.content.model.HomeSectionKind;
import com.asrevo.cvhome.content.model.common.PersistableContent;
import com.asrevo.cvhome.content.model.menu.MenuTarget;

import lombok.Getter;
import lombok.Setter;

/**
 * A block on the store's home page. Ordered by {@code sortOrder}, which the reorder endpoint rewrites.
 */
@Getter
@Setter
public class PersistableSection extends PersistableContent {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private HomeSectionKind kind;

    @Size(max = 120)
    private String targetValue;

    private Long mediaId;

    private Integer itemLimit;

    @Size(max = 40)
    private String layout;

    private MenuTarget cta;

}
