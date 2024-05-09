package com.asrevo.cvhome.store.core.model.catalog.product;

import com.asrevo.cvhome.commons.domain.Entity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serial;

@Setter
@Getter
public class PersistableImage extends Entity {


    @Serial
    private static final long serialVersionUID = 1L;
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
