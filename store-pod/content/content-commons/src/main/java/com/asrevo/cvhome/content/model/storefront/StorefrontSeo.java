package com.asrevo.cvhome.content.model.storefront;

import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorefrontSeo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String metaTitle;

    private String metaDescription;

    private String keywords;

    private String canonicalUrl;

    private boolean noindex;

    private String ogImageUrl;

}
