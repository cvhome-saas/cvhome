package com.asrevo.cvhome.content.model.page;

import java.io.Serial;

import com.asrevo.cvhome.content.model.common.PersistableContent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersistablePage extends PersistableContent {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long parentId;

    /**
     * Listed in the storefront footer ("Company" links). New with the platform.
     */
    private boolean showInFooter;

}
