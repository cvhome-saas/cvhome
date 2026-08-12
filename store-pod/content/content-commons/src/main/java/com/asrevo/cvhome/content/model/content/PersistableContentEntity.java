package com.asrevo.cvhome.content.model.content;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableContentEntity extends ContentEntity implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private List<ContentDescriptionEntity> descriptions = new ArrayList<>();

}
