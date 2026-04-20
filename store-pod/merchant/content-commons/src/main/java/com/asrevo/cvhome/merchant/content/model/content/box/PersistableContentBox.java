package com.asrevo.cvhome.merchant.content.model.content.box;

import java.io.Serial;
import java.util.List;

import com.asrevo.cvhome.merchant.content.model.content.common.ContentDescription;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableContentBox extends ContentBox {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private List<ContentDescription> descriptions;

}
