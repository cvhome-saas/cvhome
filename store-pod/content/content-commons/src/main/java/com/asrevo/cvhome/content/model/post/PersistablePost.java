package com.asrevo.cvhome.content.model.post;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Size;

import com.asrevo.cvhome.content.model.common.PersistableContent;

import lombok.Getter;
import lombok.Setter;

/**
 * A blog post ({@code /blog/<slug>}). Title, body, excerpt and SEO live on the translations; the rest here.
 */
@Getter
@Setter
public class PersistablePost extends PersistableContent {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long heroMediaId;

    private List<Long> categoryIds = new ArrayList<>();

    private List<@Size(max = 40) String> tags = new ArrayList<>();

    @Size(max = 120)
    private String authorName;

    private boolean featured;

}
