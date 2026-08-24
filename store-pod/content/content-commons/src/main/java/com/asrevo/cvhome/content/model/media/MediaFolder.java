package com.asrevo.cvhome.content.model.media;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MediaFolder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotEmpty
    @Size(max = 60)
    private String name;

    private String key;

    private Integer position;

    private boolean system;

    private long fileCount;

}
