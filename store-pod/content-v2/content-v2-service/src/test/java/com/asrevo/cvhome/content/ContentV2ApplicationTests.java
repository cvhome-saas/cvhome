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
@ActiveProfiles("test-stores")
@Tag("integration-test")
class ContentV2ApplicationTests {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    ContentV2ApplicationTests(JdbcTemplate jdbcTemplate) {
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
        assertThat(tables).contains("content", "content_description", "content_banner", "content_faq",
                "sm_sequencer");
        Integer merchantSchemaCount = jdbcTemplate.queryForObject("""
                select count(*)
                  from information_schema.schemata
                 where schema_name = 'merchant'
                """, Integer.class);
        assertThat(merchantSchemaCount).isZero();
    }

    @Test
    void initializesRepresentativeContentForEveryDemoStore() {
        var seededStores = jdbcTemplate.queryForList("""
                select store_merchant_id
                  from content.content
                 where content_id between 1101 and 1406
                 group by store_merchant_id
                having count(*) = 6
                 order by store_merchant_id
                """, String.class);
        assertThat(seededStores).containsExactly(
                "65f020632bc46470c104b76f",
                "65f023632bc26470c104b75f",
                "65f023632bc46470c104b75f",
                "65f023632bc46470c104b76f");

        Integer seededDomainRows = jdbcTemplate.queryForObject("""
                select count(*) from (
                    select content_id from content.content_page
                    union all select content_id from content.content_post
                    union all select content_id from content.content_banner
                    union all select content_id from content.content_faq
                    union all select content_id from content.content_menu
                    union all select content_id from content.content_policy
                ) seeded_domain
                where content_id between 1101 and 1406
                """, Integer.class);
        assertThat(seededDomainRows).isEqualTo(24);
    }

}
