package com.asrevo.cvhome.content.model.page;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PageBlockSpec.RichText.class, name = "RICH_TEXT"),
        @JsonSubTypes.Type(value = PageBlockSpec.Image.class, name = "IMAGE"),
        @JsonSubTypes.Type(value = PageBlockSpec.Gallery.class, name = "GALLERY"),
        @JsonSubTypes.Type(value = PageBlockSpec.VideoLink.class, name = "VIDEO_LINK"),
        @JsonSubTypes.Type(value = PageBlockSpec.ProductGrid.class, name = "PRODUCT_GRID"),
        @JsonSubTypes.Type(value = PageBlockSpec.Reference.class, name = "REFERENCE"),
        @JsonSubTypes.Type(value = PageBlockSpec.HtmlEmbed.class, name = "HTML_EMBED"),
        @JsonSubTypes.Type(value = PageBlockSpec.Spacer.class, name = "SPACER"),
        @JsonSubTypes.Type(value = PageBlockSpec.Cta.class, name = "CTA")
})
public sealed interface PageBlockSpec permits PageBlockSpec.RichText, PageBlockSpec.Image, PageBlockSpec.Gallery,
        PageBlockSpec.VideoLink, PageBlockSpec.ProductGrid, PageBlockSpec.Reference, PageBlockSpec.HtmlEmbed,
        PageBlockSpec.Spacer, PageBlockSpec.Cta {
    record RichText(String html) implements PageBlockSpec {
    }

    record Image(Long mediaId, String altText) implements PageBlockSpec {
    }

    record Gallery(List<Long> mediaIds) implements PageBlockSpec {
    }

    record VideoLink(String url) implements PageBlockSpec {
    }

    record ProductGrid(List<String> productIds) implements PageBlockSpec {
    }

    record Reference(String referenceType, String referenceId) implements PageBlockSpec {
    }

    record HtmlEmbed(String html) implements PageBlockSpec {
    }

    record Spacer(int height) implements PageBlockSpec {
    }

    record Cta(String label, String url) implements PageBlockSpec {
    }
}
