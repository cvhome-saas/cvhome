package com.asrevo.cvhome.content.model.faq;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

/**
 * One row of {@code PATCH /faq/reorder}: put entry {@code id} into {@code groupId} at {@code position}.
 */
@Getter
@Setter
public class FaqReorder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private Long id;

    @NotNull
    private Long groupId;

    @NotNull
    private Integer position;

}
