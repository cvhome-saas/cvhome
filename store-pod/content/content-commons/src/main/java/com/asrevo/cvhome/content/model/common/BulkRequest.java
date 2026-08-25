package com.asrevo.cvhome.content.model.common;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.asrevo.cvhome.content.model.BulkAction;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BulkRequest implements Serializable {

    public static final int MAX_IDS = 200;

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty
    private List<Long> ids;

    @NotNull
    private BulkAction action;

}
