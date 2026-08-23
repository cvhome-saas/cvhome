package com.asrevo.cvhome.content.model.storefront;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SitemapEntry implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String loc;

    private Instant lastmod;

    private String changefreq;

    private String type;

}
