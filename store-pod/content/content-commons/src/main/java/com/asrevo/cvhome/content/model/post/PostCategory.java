package com.asrevo.cvhome.content.model.post;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

/**
 * A blog category: a slug plus a name per locale.
 */
@Getter
@Setter
public class PostCategory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotEmpty
    @Size(max = 60)
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$")
    private String slug;

    @NotEmpty
    private Map<String, String> names = new LinkedHashMap<>();

    private Integer position;

    private long postCount;

}
