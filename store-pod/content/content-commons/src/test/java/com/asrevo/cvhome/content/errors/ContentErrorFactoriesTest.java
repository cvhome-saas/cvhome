package com.asrevo.cvhome.content.errors;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.FieldError;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The error contract these exceptions carry: the code a client switches on, the category that decides the HTTP
 * status, and the parameters the console needs to render the message itself.
 *
 * <p>
 * Content that exists but belongs to another store is reported as missing rather than as forbidden, so a caller
 * cannot probe another seller's data — that is why every not-found factory takes the store.
 * </p>
 */
class ContentErrorFactoriesTest {

    private static final String TERMS_SLUG = "terms";

    private static final String ABOUT_SLUG = "about";

    private static final String MAIN = "MAIN";

    private static final String NO_KIND = "no kind";

    private static final String UPLOAD = "upload";

    private static final String STORE = "65f023632bc46470c104b76f";

    private static final String TERMS = "TERMS";

    @Test
    void everyNotFoundVariantCarriesItsIdentifierAndTheStore() {
        assertThat(ContentNotFoundException.byId(1L, STORE).payload().params())
                .containsEntry("id", 1L).containsEntry("store", STORE);
        assertThat(ContentNotFoundException.byCode(TERMS_SLUG, STORE).payload().params()).containsEntry("code", TERMS_SLUG);
        assertThat(ContentNotFoundException.byName(ABOUT_SLUG, STORE).payload().params())
                .containsEntry("name", ABOUT_SLUG);
        assertThat(ContentNotFoundException.faqGroup(2L, STORE).payload().errorCode())
                .isEqualTo(ContentErrors.FAQ_GROUP_NOT_FOUND);
        assertThat(ContentNotFoundException.media(3L, STORE).payload().errorCode())
                .isEqualTo(ContentErrors.MEDIA_NOT_FOUND);
        assertThat(ContentNotFoundException.mediaFolder(4L, STORE).payload().errorCode())
                .isEqualTo(ContentErrors.MEDIA_NOT_FOUND);
    }

    @Test
    void everyConflictNamesWhatCollidedAndRendersAsAConflict() {
        assertThat(ContentConflictException.slugDuplicate("PAGE", ABOUT_SLUG, STORE).payload())
                .satisfies(p -> {
                    assertThat(p.errorCode()).isEqualTo(ContentErrors.SLUG_DUPLICATE);
                    assertThat(p.errorCode().category()).isEqualTo(ErrorCategory.CONFLICT);
                    assertThat(p.params()).containsEntry("slug", ABOUT_SLUG);
                });
        assertThat(ContentConflictException.versionConflict(1L, 2, 3).payload().params())
                .containsEntry("sentVersion", 2).containsEntry("currentVersion", 3);
        assertThat(ContentConflictException.pageReferenced(1L, List.of(MAIN)).payload().params())
                .containsEntry("menus", List.of(MAIN));
        assertThat(ContentConflictException.policyTypeActive(TERMS, 9L, STORE).payload().params())
                .containsEntry("existingId", 9L);
        assertThat(ContentConflictException.mediaReferenced(1L, List.of("a", "b")).getMessage())
                .contains("2 item(s)");
        assertThat(ContentConflictException.folderNotEmpty(1L, 4L).payload().params())
                .containsEntry("fileCount", 4L);
    }

    @Test
    void everyRuleFailureRendersAsUnprocessable() {
        assertThat(ContentRuleException.transitionNotAllowed(1L, "ARCHIVED", "REVIEW").payload().errorCode()
                .category()).isEqualTo(ErrorCategory.UNPROCESSABLE);
        assertThat(ContentRuleException.publishIncomplete(1L,
                List.of(FieldError.of("translations", ContentErrors.PUBLISH_INCOMPLETE, "missing")))
                .payload().fieldErrors()).hasSize(1);
        assertThat(ContentRuleException.bannerCapacity("HERO", 1, 7L).payload().params())
                .containsEntry("capacity", 1).containsEntry("conflictingId", 7L);
        assertThat(ContentRuleException.menuDepth(MAIN).payload().errorCode())
                .isEqualTo(ContentErrors.MENU_DEPTH_EXCEEDED);
        assertThat(ContentRuleException.policyVersionImmutable(1L, 2).payload().errorCode())
                .isEqualTo(ContentErrors.POLICY_VERSION_IMMUTABLE);
    }

    @Test
    void everyBadRequestNamesTheFieldOrTheFigures() {
        assertThat(InvalidContentRequestException.scheduleInvalid("too soon").payload().fieldErrors())
                .extracting(FieldError::field).containsExactly("publishAt");
        assertThat(InvalidContentRequestException.menuTargetInvalid(NO_KIND).getMessage()).contains(NO_KIND);
        assertThat(InvalidContentRequestException.bulkTooLarge(500, 100).payload().params())
                .containsEntry("size", 500).containsEntry("max", 100);
        assertThat(InvalidContentRequestException.mediaTypeNotAllowed("x.exe", "application/x-msdownload")
                .payload().errorCode()).isEqualTo(ContentErrors.MEDIA_TYPE_NOT_ALLOWED);
        assertThat(InvalidContentRequestException.mediaUnreadable("x.png", new java.io.IOException("boom")))
                .hasCauseInstanceOf(java.io.IOException.class);
    }

    @Test
    void aLimitBreachCarriesTheFiguresTheConsoleShows() {
        assertThat(MediaLimitException.tooLarge("big.png", 900L, 500L).payload())
                .satisfies(p -> {
                    assertThat(p.errorCode()).isEqualTo(ContentErrors.MEDIA_TOO_LARGE);
                    assertThat(p.params()).containsEntry("bytes", 900L).containsEntry("maxBytes", 500L);
                });
        assertThat(MediaLimitException.quotaExceeded(400L, 500L, 200L).payload().params())
                .containsEntry("bytesUsed", 400L).containsEntry("bytesQuota", 500L)
                .containsEntry("bytesRequested", 200L);
    }

    @Test
    void aStorageFailureKeepsTheOperationTheKeyAndTheCause() {
        RuntimeException cause = new IllegalStateException("denied");

        MediaStorageException e = MediaStorageException.of(UPLOAD, "files/s/media/1/a.png", cause);

        assertThat(e.payload().errorCode()).isEqualTo(ContentErrors.MEDIA_STORAGE_FAILED);
        assertThat(e.payload().params()).containsEntry("operation", UPLOAD);
        assertThat(e).hasCause(cause);
    }

    @Test
    void everyErrorCodeIsNamespacedAndUnique() {
        assertThat(ContentErrors.values()).extracting(ContentErrors::code).doesNotHaveDuplicates()
                .allSatisfy(code -> assertThat(code).contains("."));
    }

}
