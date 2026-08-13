package com.asrevo.cvhome.content;

import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Tag("integration-test")
class ContentV2SchemaTest {

    private static final Set<String> EXPECTED_TABLES = Set.of(
            "banner_artwork",
            "banner_country",
            "content",
            "content_audit",
            "content_banner",
            "content_banner_description",
            "content_description",
            "content_faq",
            "content_menu",
            "content_page",
            "content_policy",
            "content_post",
            "content_post_category",
            "content_post_description",
            "content_post_tag",
            "content_preview_token",
            "content_redirect",
            "content_revision",
            "content_search_document",
            "content_status_audit",
            "faq_group",
            "faq_group_description",
            "faq_reference",
            "media_asset",
            "media_asset_description",
            "media_asset_tag",
            "media_folder",
            "media_tag",
            "media_usage",
            "media_variant",
            "menu_item",
            "menu_item_description",
            "outbox_instance",
            "outbox_partition",
            "outbox_record",
            "page_block",
            "page_block_description",
            "policy_display_location",
            "post_category",
            "post_category_description",
            "post_tag",
            "sm_sequencer"
    );

    private static final Set<String> CRITICAL_CONSTRAINTS = Set.of(
            "content_description_content_fk",
            "content_description_route_unique",
            "content_description_translation_state_check",
            "content_policy_store_type_version_unique",
            "content_status_check",
            "content_store_code_unique",
            "content_type_check",
            "media_asset_kind_check",
            "media_asset_processing_status_check",
            "media_asset_store_checksum_unique",
            "media_usage_logical_unique",
            "page_block_type_check"
    );

    private static final Set<String> CRITICAL_INDEXES = Set.of(
            "content_description_name_trgm_idx",
            "content_description_sef_url_trgm_idx",
            "content_policy_one_active_type_unique",
            "content_publish_due_idx",
            "content_search_text_trgm_idx",
            "content_search_vector_idx",
            "content_store_status_idx",
            "content_unpublish_due_idx",
            "media_asset_store_status_idx"
    );

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createsEveryPlannedTable() {
        Set<String> actual = Set.copyOf(jdbcTemplate.queryForList("""
                select table_name
                  from information_schema.tables
                 where table_schema = 'content'
                   and table_type = 'BASE TABLE'
                """, String.class));

        assertThat(actual).containsExactlyInAnyOrderElementsOf(EXPECTED_TABLES);
    }

    @Test
    void createsCriticalConstraintsAndIndexes() {
        Set<String> constraints = Set.copyOf(jdbcTemplate.queryForList("""
                select constraint_name
                  from information_schema.table_constraints
                 where constraint_schema = 'content'
                """, String.class));
        Set<String> indexes = Set.copyOf(jdbcTemplate.queryForList("""
                select indexname
                  from pg_indexes
                 where schemaname = 'content'
                """, String.class));

        assertThat(constraints).containsAll(CRITICAL_CONSTRAINTS);
        assertThat(indexes).containsAll(CRITICAL_INDEXES);
    }

    @Test
    void pageBlockConstraintAcceptsPageReferences() {
        String definition = jdbcTemplate.queryForObject("""
                select pg_get_constraintdef(oid)
                  from pg_constraint
                 where conname = 'page_block_type_check'
                """, String.class);

        assertThat(definition).contains("PAGE_REFERENCE");
    }

    @Test
    void createsMultilingualSearchAndOutboxInfrastructure() {
        Integer extensionCount = jdbcTemplate.queryForObject(
                "select count(*) from pg_extension where extname = 'pg_trgm'", Integer.class);
        String vectorExpression = jdbcTemplate.queryForObject("""
                select generation_expression
                  from information_schema.columns
                 where table_schema = 'content'
                   and table_name = 'content_search_document'
                   and column_name = 'search_vector'
                """, String.class);
        Integer sequencerRows = jdbcTemplate.queryForObject(
                "select count(*) from content.sm_sequencer", Integer.class);

        assertThat(extensionCount).isOne();
        assertThat(vectorExpression).contains("to_tsvector('simple'");
        assertThat(sequencerRows).isEqualTo(21);
    }

}
