package com.asrevo.cvhome.content.service;

import java.time.Instant;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.TestcontainersConfiguration;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.ContentVersionConflictException;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.ContentType;
import com.asrevo.cvhome.content.model.ContentView;
import com.asrevo.cvhome.content.model.ContentWriteRequest;
import com.asrevo.cvhome.content.model.LifecycleRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Tag("integration-test")
@Transactional
class ContentV2ServiceIntegrationTest {
    private static final StoreMerchantId STORE = new StoreMerchantId("test-store-one");
    private static final StoreMerchantId OTHER_STORE = new StoreMerchantId("test-store-two");
    private static final LanguageCode ENGLISH = new LanguageCode("en");
    private static final String ACTOR = "tester";
    private static final String CODE = "integration-page";
    private static final String BODY = "body";
    private static final String RENAMED = "renamed";
    private static final String UPDATED_NAME = "second";

    @Autowired
    private ContentV2Service service;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private EntityManager entityManager;

    @Test
    void createsUpdatesAndKeepsLookupTenantScoped() throws Exception {
        ContentView created = service.create(STORE, ENGLISH, request("first"), ACTOR);
        ContentView updated = service.update(STORE, ENGLISH, created.id(), created.version(), request(UPDATED_NAME),
                ACTOR);

        assertThat(updated.translations()).singleElement()
                .satisfies(it -> assertThat(it.name()).isEqualTo(UPDATED_NAME));
        assertThatThrownBy(() -> service.find(OTHER_STORE, created.id()))
                .isInstanceOf(ContentNotFoundException.class);
        assertThatThrownBy(() -> service.update(STORE, ENGLISH, created.id(), created.version(), request("stale"),
                ACTOR))
                .isInstanceOf(ContentVersionConflictException.class);
    }

    @Test
    void writesAuditStatusHistoryRedirectsAndProcessesDueLifecycle() throws Exception {
        ContentView created = service.create(STORE, ENGLISH, request("audited"), ACTOR);
        ContentView published = service.transition(STORE, created.id(), created.version(), ContentStatus.PUBLISHED,
                new LifecycleRequest(null, null, "publish-test"), ACTOR);
        ContentWriteRequest renamed = new ContentWriteRequest(CODE, ContentType.PAGE, RENAMED, RENAMED,
                BODY, "renamed-route", null, null, null, null, false, null, null);
        service.update(STORE, ENGLISH, published.id(), published.version(), renamed, ACTOR);
        entityManager.flush();

        assertThat(countAudit(created.id())).isEqualTo(3);
        assertThat(countStatusAudit(created.id())).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("""
                select count(*) from content.content_redirect
                 where destination_content_id = ? and old_path = ?
                """, Integer.class, created.id(), CODE)).isEqualTo(1);

        ContentView scheduled = service.create(STORE, ENGLISH, request("scheduled-page", "scheduled"), ACTOR);
        ContentView pending = service.transition(STORE, scheduled.id(), scheduled.version(), ContentStatus.SCHEDULED,
                new LifecycleRequest(Instant.now().plusSeconds(60), null, "schedule-test"), ACTOR);
        jdbcTemplate.update("""
                update content.content
                   set publish_at = current_timestamp - interval '1 second'
                 where content_id = ?
                """, pending.id());
        entityManager.clear();

        assertThat(service.processDueContent()).isEqualTo(1);
        assertThat(service.find(STORE, pending.id()).status()).isEqualTo(ContentStatus.PUBLISHED);
        assertThat(service.processDueContent()).isZero();
    }

    private int countAudit(Long contentId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from content.content_audit where content_id = ?", Integer.class, contentId);
    }

    private int countStatusAudit(Long contentId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from content.content_status_audit where content_id = ?", Integer.class,
                contentId);
    }

    private static ContentWriteRequest request(String name) {
        return request(CODE, name);
    }

    private static ContentWriteRequest request(String code, String name) {
        return new ContentWriteRequest(code, ContentType.PAGE, name, name, BODY, code,
                null, null, null, null, false, null, null);
    }
}
