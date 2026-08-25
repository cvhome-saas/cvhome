package com.asrevo.cvhome.content.model.storefront;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Grouped, published FAQ entries plus the {@code FAQPage} JSON-LD document the storefront can embed.
 */
@Getter
@Setter
public class StorefrontFaq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String servedLocale;

    private List<Group> groups;

    private String jsonLd;

    @Getter
    @Setter
    public static class Group implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String key;

        private String name;

        private List<Entry> entries;

    }

    @Getter
    @Setter
    public static class Entry implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private Long id;

        private String slug;

        private String question;

        private String answer;

        private boolean showInCheckoutHelp;

    }

}
