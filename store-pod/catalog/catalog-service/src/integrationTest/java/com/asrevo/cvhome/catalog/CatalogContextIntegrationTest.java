package com.asrevo.cvhome.catalog;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

@StorageIntegrationTest
class CatalogContextIntegrationTest {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    CatalogContextIntegrationTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void contextLoads() {
    }

    /**
     * The seeded photos name a path in the bucket and no host.
     *
     * <p>
     * They used to carry a whole url, so every environment came up serving the demo catalogue from one
     * developer's MinIO — and a second local stack, whose MinIO is port-shifted, could not fetch them at all.
     * The url is composed when a product is read; what the seed writes has to stay free of an address for that
     * to mean anything.
     * </p>
     */
    @Test
    void seededImagesStoreAPathAndNoHost() {
        List<String> paths = jdbcTemplate.queryForList("""
                select image_url
                  from catalog.product_image
                 where image_url is not null
                """, String.class);
        assertThat(paths).isNotEmpty().allSatisfy(path -> assertThat(path).doesNotContain("://"));
    }

}
