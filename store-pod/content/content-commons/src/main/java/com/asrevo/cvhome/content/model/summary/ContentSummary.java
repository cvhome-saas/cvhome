package com.asrevo.cvhome.content.model.summary;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * The four KPI cards and the seven rail counts of the Content management hub, computed with the same predicates the
 * list endpoints use so the two never disagree.
 */
@Getter
@Setter
public class ContentSummary implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private long publishedItems;

    private Drafts drafts = new Drafts();

    private AwaitingTranslation awaitingTranslation = new AwaitingTranslation();

    private Media media = new Media();

    private Map<String, Long> counts;

    @Getter
    @Setter
    public static class Drafts implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private long total;

        private long staleOver30Days;

    }

    @Getter
    @Setter
    public static class AwaitingTranslation implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private long total;

        private Map<String, Long> byLocale;

    }

    @Getter
    @Setter
    public static class Media implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private long bytesUsed;

        private long bytesQuota;

        private long fileCount;

    }

}
