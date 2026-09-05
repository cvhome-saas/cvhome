package com.asrevo.cvhome.content.api.v1;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.v1.support.Actors;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.page.PersistablePage;
import com.asrevo.cvhome.content.service.ContentItemService;
import com.asrevo.cvhome.content.service.ContentTypeBinding;
import com.asrevo.cvhome.content.service.ListQuery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * The workflow every content type shares, and who each change is recorded as.
 *
 * <p>
 * One base class serves banners, posts, FAQs, policies and sections, so a mistake here is a mistake in all of
 * them. Two details carry the weight. The {@code locale} parameter has three meanings -- absent, blank and the
 * literal {@code all} all mean "every locale" and must become a null filter rather than a search for a language
 * called "all". And every mutation stamps {@link Actors#current()}, which walks a claim precedence chain before
 * falling back to the subject; an authorship field that silently records "system" for a signed-in editor is a
 * thing nobody notices until an audit asks who changed a published page.
 * </p>
 */
class WorkflowContentApiTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");
    private static final LanguageCode ENGLISH = new LanguageCode("en");
    private static final String EDITOR = "editor@example.com";
    private static final String SUBJECT = "sub-1";
    private static final String NAME_CLAIM = "name";
    private static final String EMAIL_CLAIM = "email";
    private static final String PREFERRED = "preferred";
    private static final String ARABIC = "ar";
    private static final String SLUG = "a-slug";

    private final ContentItemService items = Mockito.mock(ContentItemService.class);
    @SuppressWarnings("unchecked")
    private final ContentTypeBinding<PersistablePage, PersistablePage> binding = Mockito.mock(ContentTypeBinding.class);
    private final TestApi api = new TestApi(items, binding);

    /** A concrete subclass, because the workflow lives entirely in the abstract base. */
    private static final class TestApi extends WorkflowContentApi<PersistablePage, PersistablePage> {
        private TestApi(ContentItemService items, ContentTypeBinding<PersistablePage, PersistablePage> binding) {
            super(items, binding);
        }
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private static Jwt jwtWith(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("token").header("alg", "none").subject(SUBJECT)
                .issuedAt(Instant.EPOCH).expiresAt(Instant.EPOCH.plusSeconds(3600));
        claims.forEach(builder::claim);
        return builder.build();
    }

    private static void signIn(Map<String, Object> claims) {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwtWith(claims),
                List.of(new SimpleGrantedAuthority("SCOPE_store_pod"))));
    }

    @ParameterizedTest
    @CsvSource({"name,by-name", "full_name,by-full-name", "preferred_username,by-username", "email,by-email"})
    void theActorIsTheFirstNameClaimThatCarriesAValue(String claim, String value) {
        signIn(Map.of(claim, value));

        assertThat(Actors.current()).isEqualTo(value);
    }

    @Test
    void nameOutranksTheOtherClaimsWhenSeveralArePresent() {
        signIn(Map.of(NAME_CLAIM, PREFERRED, EMAIL_CLAIM, "fallback@example.com"));

        assertThat(Actors.current()).isEqualTo(PREFERRED);
    }

    @Test
    void aTokenWithNoUsableNameClaimFallsBackToItsSubject() {
        signIn(Map.of("unrelated", "x"));

        assertThat(Actors.current()).isEqualTo(SUBJECT);
    }

    @Test
    void aBlankNameClaimIsSkippedRatherThanRecordedAsTheActor() {
        signIn(Map.of(NAME_CLAIM, "   ", EMAIL_CLAIM, EDITOR));

        assertThat(Actors.current()).isEqualTo(EDITOR);
    }

    @Test
    void aNonJwtPrincipalIsRecordedByItsNameAndNothingAtAllIsTheSystem() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(EDITOR, null, List.of()));
        assertThat(Actors.current()).isEqualTo(EDITOR);

        SecurityContextHolder.clearContext();
        assertThat(Actors.current()).isEqualTo("system");
    }

    @ParameterizedTest
    @CsvSource({"all", "ALL", "''", "'   '"})
    void everyShapeOfEveryLocaleBecomesANullFilter(String locale) {
        api.list(STORE, ENGLISH, ContentStatus.DRAFT, locale, null, null, null);

        ArgumentCaptor<ListQuery> query = ArgumentCaptor.forClass(ListQuery.class);
        verify(items).list(any(), eq(STORE), eq(ENGLISH), query.capture(), any());
        assertThat(query.getValue().locale()).isNull();
    }

    @Test
    void anActualLocaleIsPassedThroughAsALanguageCode() {
        api.list(STORE, ENGLISH, null, ARABIC, null, null, null);

        ArgumentCaptor<ListQuery> query = ArgumentCaptor.forClass(ListQuery.class);
        verify(items).list(any(), eq(STORE), eq(ENGLISH), query.capture(), any());
        assertThat(query.getValue().locale()).isEqualTo(new LanguageCode(ARABIC));
    }

    @Test
    void anAbsentLocaleIsAlsoANullFilter() {
        api.list(STORE, ENGLISH, null, null, null, null, null);

        ArgumentCaptor<ListQuery> query = ArgumentCaptor.forClass(ListQuery.class);
        verify(items).list(any(), eq(STORE), eq(ENGLISH), query.capture(), any());
        assertThat(query.getValue().locale()).isNull();
    }

    @Test
    void readsAndDeletesAreScopedToTheStore() throws Exception {
        api.get(STORE, ENGLISH, 1L);
        api.revisions(STORE, ENGLISH, 1L);
        api.delete(STORE, ENGLISH, 1L, true);
        api.slugAvailable(STORE, ENGLISH, SLUG, 2L);

        verify(items).get(binding, 1L, STORE);
        verify(items).revisions(binding, 1L, STORE);
        verify(items).delete(binding, 1L, STORE, true);
        verify(items).slugAvailable(STORE, SLUG, 2L);
    }

    @Test
    void everyTransitionNamesItsTargetStatusAndItsActor() throws Exception {
        signIn(Map.of(NAME_CLAIM, EDITOR));

        api.publish(STORE, ENGLISH, 1L, null);
        api.unpublish(STORE, ENGLISH, 1L);
        api.submitReview(STORE, ENGLISH, 1L);
        api.archive(STORE, ENGLISH, 1L);
        api.restoreArchived(STORE, ENGLISH, 1L);

        verify(items).transition(binding, 1L, STORE, ContentStatus.PUBLISHED, null, ENGLISH, EDITOR);
        verify(items).transition(binding, 1L, STORE, ContentStatus.REVIEW, null, ENGLISH, EDITOR);
        verify(items).transition(binding, 1L, STORE, ContentStatus.ARCHIVED, null, ENGLISH, EDITOR);
        // Unpublish and restore both land on DRAFT, which is why they are two calls to the same status.
        verify(items, Mockito.times(2))
                .transition(binding, 1L, STORE, ContentStatus.DRAFT, null, ENGLISH, EDITOR);
    }

    @Test
    void createUpdateRestoreAndTranslationAllRecordTheActor() throws Exception {
        signIn(Map.of(NAME_CLAIM, EDITOR));
        PersistablePage body = new PersistablePage();

        api.create(STORE, ENGLISH, body);
        api.update(STORE, ENGLISH, 1L, body);
        api.restoreRevision(STORE, ENGLISH, 1L, 3);
        api.translation(STORE, ENGLISH, 1L, ARABIC, null);

        verify(items).create(binding, body, STORE, ENGLISH, EDITOR);
        verify(items).update(binding, 1L, body, STORE, ENGLISH, EDITOR);
        verify(items).restore(binding, 1L, 3, STORE, ENGLISH, EDITOR);
        verify(items).updateTranslation(binding, 1L, new LanguageCode(ARABIC), null, STORE, EDITOR);
    }

    @Test
    void aBulkRequestIsForwardedWholeWithItsActor() throws Exception {
        signIn(Map.of(NAME_CLAIM, EDITOR));

        api.bulk(STORE, ENGLISH, null);

        verify(items).bulk(binding, null, STORE, ENGLISH, EDITOR);
    }
}
