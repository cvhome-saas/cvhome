package com.asrevo.cvhome.content;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles({"signer", "test-stores"})
@Tag("integration-test")
class ContentApplicationTests {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    ContentApplicationTests(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
    void legacySeedRowsArePublishedByTheMigration() {
        Integer drafts = jdbcTemplate.queryForObject("""
                select count(*) from content.content where visible = true and status <> 'PUBLISHED'
                """, Integer.class);
        assertThat(drafts).isZero();
        Integer pages = jdbcTemplate.queryForObject("""
                select count(*) from content.content
                 where store_merchant_id = '65f023632bc26470c104b75f' and content_type = 'PAGE' and status = 'PUBLISHED'
                """, Integer.class);
        assertThat(pages).isEqualTo(6);
    }

}
