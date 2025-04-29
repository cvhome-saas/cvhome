package com.asrevo.cvhome.catalog.model.product;

import com.asrevo.cvhome.commons.domain.Entity;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
public class PersistableImage extends Entity {

    @Serial private static final long serialVersionUID = 1L;
    private boolean defaultImage;
    private int imageType = 0;
    private String name = null;
    private String path = null;

    private MultipartFile[] files;
    private byte[] bytes = null;
    private String contentType = null;

    /**
     * An external image url
     */
    private String imageUrl = null;
}
