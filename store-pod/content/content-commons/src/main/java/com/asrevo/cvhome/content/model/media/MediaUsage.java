package com.asrevo.cvhome.content.model.media;

import java.io.Serial;
import java.io.Serializable;

import com.asrevo.cvhome.store.core.entity.content.ContentType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MediaUsage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private ContentType itemType;

    private Long itemId;

    private String itemTitle;

    private String field;

}
