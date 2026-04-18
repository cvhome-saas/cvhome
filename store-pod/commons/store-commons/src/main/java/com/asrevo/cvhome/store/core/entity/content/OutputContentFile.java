package com.asrevo.cvhome.store.core.entity.content;

import java.io.ByteArrayOutputStream;
import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * Data class responsible for carrying out static content data from Infispan cache to
 * service layer.
 *
 * @author Umesh Awasthi
 * @since 1.2
 */
@Setter
@Getter
public class OutputContentFile extends StaticContentFile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private ByteArrayOutputStream file;

}
