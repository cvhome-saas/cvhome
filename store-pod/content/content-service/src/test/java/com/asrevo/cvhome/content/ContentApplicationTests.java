package com.asrevo.cvhome.content;

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
        assertThat(tables).containsExactly("content", "content_description", "sm_sequencer");
        Integer merchantSchemaCount = jdbcTemplate.queryForObject("""
                select count(*)
                  from information_schema.schemata
                 where schema_name = 'merchant'
                """, Integer.class);
        assertThat(merchantSchemaCount).isZero();
    }

}
