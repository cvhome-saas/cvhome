package com.asrevo.cvhome.content;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.service.MediaService;
import com.asrevo.cvhome.s2s.model.CdnProperties;
import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.security.Tokens;

import static org.assertj.core.api.Assertions.assertThat;

@StorageIntegrationTest
class ContentContextIntegrationTest {

    /** Demo stores under `init-sql/stores/`, each seeded with the same content. */
    private static final int STORES_SEEDED = 4;

    private final JdbcTemplate jdbcTemplate;

    private final CdnProperties cdn;

    private final MediaService media;

    @Autowired
    ContentContextIntegrationTest(JdbcTemplate jdbcTemplate, CdnProperties cdn, MediaService media) {
        this.jdbcTemplate = jdbcTemplate;
        this.cdn = cdn;
        this.media = media;
    }

    @Test
    void contextLoads() {
    }

    @Test
    void initializesOnlyContentSchema() {
        var tables = jdbcTemplate.queryForList("""
                select table_name
                  from information_schema.tables
                 where table_schema = 'content'
                 order by table_name
                """, String.class);
        assertThat(tables).contains("content", "content_description", "content_revision", "content_status_audit",
                "redirect", "sm_sequencer");
        Integer merchantSchemaCount = jdbcTemplate.queryForObject("""
                select count(*)
                  from information_schema.schemata
                 where schema_name = 'merchant'
                """, Integer.class);
        assertThat(merchantSchemaCount).isZero();
    }

    @Test
    void everyUnpublishedSeedRowIsDeliberate() {
        /*
         * Nothing is left unpublished by accident.
         *
         * The demo seeds do ship rows outside PUBLISHED on purpose — a SCHEDULED page and a DRAFT page and
         * post — so the console's status filters and the storefront's `servable` gate have something real
         * to act on. What must not happen is a seed that simply forgot to publish itself, and that shows
         * up as a *third* state rather than as a count, which is why this asserts the set of states rather
         * than how many rows are in them: a count would need editing every time the seeds gain a page.
         *
         * ARCHIVED is absent by construction, not by omission: archiving clears `visible`, so an archived
         * seed row never reaches this query. That is the invariant, and if archiving ever stopped hiding
         * a row it would surface here as the unexpected third state.
         */
        var states = jdbcTemplate.queryForList("""
                select distinct status
                  from content.content
                 where visible = true and status <> 'PUBLISHED'
                 order by status
                """, String.class);
        assertThat(states).containsExactlyInAnyOrder("DRAFT", "SCHEDULED");

        // And every demo store carries the same set, so no store is the one that quietly has nothing to filter.
        var perStore = jdbcTemplate.queryForList("""
                select count(distinct status)
                  from content.content
                 where visible = true and status <> 'PUBLISHED'
                 group by store_merchant_id
                """, Integer.class);
        assertThat(perStore).hasSize(STORES_SEEDED).allMatch(count -> count == 2);

        /*
         * Every demo store gets the same content. Asserted as uniformity rather than as a number, because a
         * number here is a second place to edit every time the seeds gain a page — and the failure it
         * actually needs to catch is one store's seed file falling behind the others, which a count against
         * a single store cannot see at all.
         */
        var pagesPerStore = jdbcTemplate.queryForList("""
                select count(*) from content.content
                 where content_type = 'PAGE' and status = 'PUBLISHED'
                 group by store_merchant_id
                """, Integer.class);
        assertThat(pagesPerStore).hasSize(STORES_SEEDED);
        assertThat(pagesPerStore).allMatch(count -> count.equals(pagesPerStore.get(0)));
        assertThat(pagesPerStore.get(0)).isGreaterThanOrEqualTo(6);
    }

    /**
     * Seeded assets are served from the CDN this environment configures.
     *
     * <p>
     * The seed writes a storage key and nothing about the host; the url comes from that key and
     * {@code com.asrevo.cvhome.cdn.base-path} when the asset is read. It used to carry a whole url too, so every
     * environment came up serving the demo library from one developer's MinIO — and a second local stack, whose
     * MinIO is port-shifted, could not fetch it at all. Here the base is the MinIO container's address, which is
     * a value no script could have written down.
     * </p>
     */
    @Test
    void seededAssetsAreServedFromTheConfiguredCdn() {
        StoreMerchantId store = new StoreMerchantId(Tokens.STORE_1);
        List<Long> ids = jdbcTemplate.queryForList("""
                select id
                  from content.media_asset
                 where store_merchant_id = ?
                 order by id
                 limit 5
                """, Long.class, store.getId());
        assertThat(ids).isNotEmpty();

        assertThat(media.urls(store, ids).values())
                .isNotEmpty()
                .allSatisfy(url -> assertThat(url).startsWith(this.cdn.basePath()));
    }

}
