package com.asrevo.cvhome.content.model.faq;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Size;

import com.asrevo.cvhome.content.model.common.PersistableContent;

import lombok.Getter;
import lombok.Setter;

/**
 * One question (title) and its answer (body) per locale, in a group, at a position.
 */
@Getter
@Setter
public class PersistableFaq extends PersistableContent {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long groupId;

    private Integer position;

    private List<@Size(max = 40) String> keywords = new ArrayList<>();

    private boolean showInCheckoutHelp;

}
