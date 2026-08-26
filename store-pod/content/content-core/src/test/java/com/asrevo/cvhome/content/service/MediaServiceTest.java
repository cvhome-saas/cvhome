package com.asrevo.cvhome.content.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.MediaAsset;
import com.asrevo.cvhome.content.entity.MediaFolder;
import com.asrevo.cvhome.content.entity.MediaQuota;
import com.asrevo.cvhome.content.entity.MediaUsageRow;
import com.asrevo.cvhome.content.errors.ContentConflictException;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.InvalidContentRequestException;
import com.asrevo.cvhome.content.errors.MediaLimitException;
import com.asrevo.cvhome.content.model.MediaKind;
import com.asrevo.cvhome.content.model.MediaOwnerKind;
import com.asrevo.cvhome.content.model.media.PersistableMediaAsset;
import com.asrevo.cvhome.content.model.media.ReadableMediaAsset;
import com.asrevo.cvhome.content.model.summary.ContentSummary;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.content.repository.MediaAssetRepository;
import com.asrevo.cvhome.content.repository.MediaFolderRepository;
import com.asrevo.cvhome.content.repository.MediaQuotaRepository;
import com.asrevo.cvhome.content.repository.MediaUsageRepository;
import com.asrevo.cvhome.content.storage.MediaStorage;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The media library's rules: an accepted type, a per-file limit, a per-store byte quota, sha-256 deduplication,
 * and the usage index that turns a delete of a referenced asset into a 409.
 */
class MediaServiceTest {

    private static final String TEXT_PLAIN = "text/plain";

    private static final String OCTET_STREAM = "application/octet-stream";

    private static final String FALLBACK_NAME = "file";

    private static final String PNG_EXTENSION = ".png";

    private static final String BRAND_ASSETS_KEY = "brand-assets";

    private static final String FOLDER_FALLBACK = "folder";

    private static final String STORED_URL = "https://cdn.test/x";

    private static final String SVG = "image/svg+xml";

    private static final String ABOUT_TITLE = "About";

    private static final String OLD_TITLE = "Old";

    private static final String BRAND_KEY = "brand";

    private static final String EN = "en";

    private static final String ALT_TEXT = "The logo";

    private static final String BANNERS_KEY = "banners";

    private static final String BRAND_NAME = "Brand";

    private static final String STORE_ID = ContentFixtures.STORE.getId();

    private static final String ACTOR = "ada";

    private static final String PNG = "image/png";

    private static final String REF_TWO = "2";

    private static final String LOGO = "logo.png";

    private static final long MAX_FILE = 1024L;

    private static final long QUOTA = 4096L;

    private MediaAssetRepository assets;

    private MediaFolderRepository folders;

    private MediaUsageRepository usage;

    private MediaQuotaRepository quotas;

    private ContentRepository contents;

    private MediaStorage storage;

    private MediaService service;

    @BeforeEach
    void setUp() {
        assets = mock(MediaAssetRepository.class);
        folders = mock(MediaFolderRepository.class);
        usage = mock(MediaUsageRepository.class);
        quotas = mock(MediaQuotaRepository.class);
        contents = mock(ContentRepository.class);
        storage = mock(MediaStorage.class);
        service = new MediaService(assets, folders, usage, quotas, contents, storage, ContentFixtures.clock());
    }

    private static MediaAsset asset(Long id, String filename) {
        MediaAsset a = new MediaAsset();
        a.setId(id);
        a.setStoreMerchantId(STORE_ID);
        a.setFilename(filename);
        a.setOriginalFilename(filename);
        a.setMimeType(PNG);
        a.setKind(MediaKind.IMAGE);
        a.setBytes(100);
        a.setChecksum("abc");
        a.setStorageKey("files/s/media/1/logo.png");
        a.setPublicUrl("https://cdn.test/files/s/media/1/logo.png");
        a.setUploadedAt(ContentFixtures.NOW);
        return a;
    }

    private static MediaFolder folder(Long id, String key) {
        MediaFolder f = new MediaFolder();
        f.setId(id);
        f.setStoreMerchantId(STORE_ID);
        f.setKey(key);
        f.setName(key);
        f.setPosition(1);
        return f;
    }

    @Nested
    class MimeAndFilenameNormalisation {

        @ParameterizedTest
        @CsvSource({
            "photo.JPG, application/octet-stream, image/jpeg",
            "clip.webm, '', video/webm",
            "doc.pdf, , application/pdf",
            "archive.zip, application/zip, application/zip",
            "vector.svg, image/svg+xml; charset=utf-8, image/svg+xml",
        })
        void anUnhelpfulContentTypeFallsBackToTheExtension(String filename, String declared, String expected) {
            assertThat(MediaService.normaliseMime(filename, declared)).isEqualTo(expected);
        }

        @Test
        void anUnknownExtensionKeepsWhateverWasDeclared() {
            assertThat(MediaService.normaliseMime("notes.txt", TEXT_PLAIN)).isEqualTo(TEXT_PLAIN);
            assertThat(MediaService.normaliseMime("notes", null)).isEqualTo(OCTET_STREAM);
            assertThat(MediaService.normaliseMime(null, null)).isEqualTo(OCTET_STREAM);
        }

        @Test
        void aFilenameIsStrippedOfPathsAndUnsafeCharacters() {
            assertThat(MediaService.safeFilename("C:\\Users\\ada\\my photo!.png")).isEqualTo("my-photo-.png");
            assertThat(MediaService.safeFilename("/tmp/a/b.png")).isEqualTo("b.png");
            assertThat(MediaService.safeFilename(null)).isEqualTo(FALLBACK_NAME);
        }

        @Test
        void aDotfileNeverBecomesAHiddenFile() {
            assertThat(MediaService.safeFilename(".htaccess")).isEqualTo("file.htaccess");
            assertThat(MediaService.safeFilename("///")).isEqualTo(FALLBACK_NAME);
        }

        @Test
        void aVeryLongFilenameKeepsItsTail() {
            String name = "a".repeat(300).concat(PNG_EXTENSION);

            assertThat(MediaService.safeFilename(name)).hasSize(200).endsWith(PNG_EXTENSION);
        }

        @Test
        void slugifyFallsBackToAWordWhenNothingSurvives() {
            assertThat(MediaService.slugify("  Brand Assets! ")).isEqualTo(BRAND_ASSETS_KEY);
            assertThat(MediaService.slugify("!!!")).isEqualTo(FOLDER_FALLBACK);
            assertThat(MediaService.slugify(null)).isEqualTo(FOLDER_FALLBACK);
        }

        @Test
        void theChecksumIsAStableSha256() {
            assertThat(MediaService.sha256(new byte[0]))
                    .isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        }

    }

    @Nested
    class Uploads {

        @Test
        void anUnknownFolderIsRejectedBeforeAnythingIsStored() {
            when(folders.findByIdAndStoreMerchantId(9L, STORE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.upload(ContentFixtures.STORE, List.of(), 9L, MAX_FILE, QUOTA, ACTOR))
                    .isInstanceOf(ContentNotFoundException.class);
        }

        @Test
        void aTypeOutsideTheAllowListIsRefused() {
            when(quotas.findById(STORE_ID)).thenReturn(Optional.empty());
            var upload = new MediaService.Upload("script.sh", "text/x-shellscript", new byte[] {1});

            assertThatThrownBy(() -> service.upload(ContentFixtures.STORE, List.of(upload), null, MAX_FILE, QUOTA,
                    ACTOR))
                    .isInstanceOf(InvalidContentRequestException.class)
                    .hasMessageContaining("not an accepted file type");
        }

        @Test
        void aFileOverThePerFileLimitIsRefused() {
            when(quotas.findById(STORE_ID)).thenReturn(Optional.empty());
            var upload = new MediaService.Upload(LOGO, PNG, new byte[2048]);

            assertThatThrownBy(() -> service.upload(ContentFixtures.STORE, List.of(upload), null, MAX_FILE, QUOTA,
                    ACTOR))
                    .isInstanceOf(MediaLimitException.class)
                    .hasMessageContaining("the limit is");
        }

        @Test
        void anUploadThatWouldBreachTheQuotaIsRefused() {
            MediaQuota quota = new MediaQuota();
            quota.setStoreMerchantId(STORE_ID);
            quota.setBytesUsed(QUOTA - 1);
            when(quotas.findById(STORE_ID)).thenReturn(Optional.of(quota));
            when(assets.findByStoreMerchantIdAndChecksum(anyString(), anyString())).thenReturn(Optional.empty());
            var upload = new MediaService.Upload(LOGO, PNG, new byte[100]);

            assertThatThrownBy(() -> service.upload(ContentFixtures.STORE, List.of(upload), null, MAX_FILE, QUOTA,
                    ACTOR))
                    .isInstanceOf(MediaLimitException.class)
                    .hasMessageContaining("storage quota");
        }

        @Test
        void reUploadingTheSameBytesReturnsTheExistingAssetAndStoresNothing() throws Exception {
            when(quotas.findById(STORE_ID)).thenReturn(Optional.empty());
            when(assets.findByStoreMerchantIdAndChecksum(anyString(), anyString()))
                    .thenReturn(Optional.of(asset(5L, LOGO)));
            when(usage.countByAssetId(5L)).thenReturn(2L);
            var upload = new MediaService.Upload(LOGO, PNG, new byte[10]);

            List<ReadableMediaAsset> out = service.upload(ContentFixtures.STORE, List.of(upload), null, MAX_FILE,
                    QUOTA, ACTOR);

            assertThat(out).singleElement().satisfies(r -> {
                assertThat(r.getId()).isEqualTo(5L);
                assertThat(r.getUsageCount()).isEqualTo(2);
            });
            verify(storage, never()).put(anyString(), any(), anyString());
        }

        @Test
        void anSvgIsSanitisedBeforeItIsStored() throws Exception {
            when(quotas.findById(STORE_ID)).thenReturn(Optional.empty());
            when(assets.findByStoreMerchantIdAndChecksum(anyString(), anyString())).thenReturn(Optional.empty());
            when(assets.saveAndFlush(any())).thenAnswer(i -> {
                MediaAsset a = i.getArgument(0);
                a.setId(11L);
                return a;
            });
            when(storage.url(anyString())).thenReturn(STORED_URL);
            byte[] svg = "<svg><script>alert(1)</script></svg>".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            var upload = new MediaService.Upload("mark.svg", SVG, svg);

            service.upload(ContentFixtures.STORE, List.of(upload), null, MAX_FILE, QUOTA, ACTOR);

            var bytes = org.mockito.ArgumentCaptor.forClass(byte[].class);
            verify(storage).put(anyString(), bytes.capture(), eq(SVG));
            assertThat(new String(bytes.getValue(), java.nio.charset.StandardCharsets.UTF_8))
                    .doesNotContain("script");
        }

        @Test
        void aStoredUploadGetsAKeyThatCarriesItsIdAndBumpsTheQuota() throws Exception {
            when(quotas.findById(STORE_ID)).thenReturn(Optional.empty());
            when(assets.findByStoreMerchantIdAndChecksum(anyString(), anyString())).thenReturn(Optional.empty());
            when(assets.saveAndFlush(any())).thenAnswer(i -> {
                MediaAsset a = i.getArgument(0);
                a.setId(11L);
                return a;
            });
            when(storage.url(anyString())).thenReturn(STORED_URL);
            var upload = new MediaService.Upload(LOGO, PNG, new byte[64]);

            List<ReadableMediaAsset> out = service.upload(ContentFixtures.STORE, List.of(upload), null, MAX_FILE,
                    QUOTA, ACTOR);

            verify(storage).put(eq(MediaStorage.key(STORE_ID, 11L, LOGO)), any(), eq(PNG));
            assertThat(out).singleElement().satisfies(r -> {
                assertThat(r.getUrl()).isEqualTo(STORED_URL);
                assertThat(r.getKind()).isEqualTo(MediaKind.IMAGE);
                assertThat(r.getUploadedBy()).isEqualTo(ACTOR);
            });
            var quota = org.mockito.ArgumentCaptor.forClass(MediaQuota.class);
            verify(quotas).save(quota.capture());
            assertThat(quota.getValue().getBytesUsed()).isEqualTo(64);
            assertThat(quota.getValue().getFileCount()).isEqualTo(1);
        }

    }

    @Nested
    class ReadsAndWrites {

        @Test
        void anAssetOfAnotherStoreReadsAsMissing() {
            when(assets.findByIdAndStoreMerchantId(3L, STORE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.get(ContentFixtures.STORE, 3L))
                    .isInstanceOf(ContentNotFoundException.class);
        }

        @Test
        void readingAnAssetListsWhatUsesIt() throws Exception {
            MediaUsageRow row = new MediaUsageRow();
            row.setAssetId(5L);
            row.setOwnerKind(MediaOwnerKind.CONTENT);
            row.setOwnerRef(REF_TWO);
            row.setContentId(2L);
            row.setContentType(ContentType.PAGE);
            row.setField("og");
            when(assets.findByIdAndStoreMerchantId(5L, STORE_ID)).thenReturn(Optional.of(asset(5L, LOGO)));
            when(usage.findByAssetId(5L)).thenReturn(List.of(row));
            when(contents.findById(2L)).thenReturn(Optional.of(
                    ContentFixtures.published(2L, ContentType.PAGE, "about", ABOUT_TITLE)));

            ReadableMediaAsset out = service.get(ContentFixtures.STORE, 5L);

            assertThat(out.getUsageCount()).isEqualTo(1);
            assertThat(out.getUsage()).singleElement().satisfies(u -> {
                assertThat(u.getItemId()).isEqualTo(2L);
                assertThat(u.getItemTitle()).isEqualTo(ABOUT_TITLE);
            });
        }

        @Test
        void usageOfAnAssetWhoseItemVanishedStillLists() throws Exception {
            MediaUsageRow row = new MediaUsageRow();
            row.setAssetId(5L);
            row.setOwnerKind(MediaOwnerKind.CONTENT);
            row.setOwnerRef("99");
            row.setContentId(99L);
            when(assets.findByIdAndStoreMerchantId(5L, STORE_ID)).thenReturn(Optional.of(asset(5L, LOGO)));
            when(usage.findByAssetId(5L)).thenReturn(List.of(row));
            when(contents.findById(99L)).thenReturn(Optional.empty());

            assertThat(service.usage(ContentFixtures.STORE, 5L)).singleElement()
                    .satisfies(u -> assertThat(u.getItemTitle()).isNull());
        }

        @Test
        void urlsSkipNullsDeduplicatesAndReturnsNothingForAnEmptyRequest() {
            when(assets.findByStoreMerchantIdAndIdIn(STORE_ID, List.of(5L)))
                    .thenReturn(List.of(asset(5L, LOGO)));

            assertThat(service.urls(ContentFixtures.STORE, java.util.Arrays.asList(5L, null, 5L)))
                    .containsExactly(Map.entry(5L, asset(5L, LOGO).getPublicUrl()));
            assertThat(service.urls(ContentFixtures.STORE, java.util.Arrays.asList((Long) null))).isEmpty();
        }

        @Test
        void aNullMediaIdHasNoUrl() {
            assertThat(service.url(ContentFixtures.STORE, null)).isEmpty();
        }

        @Test
        void patchLeavesNullFieldsAlone() throws Exception {
            MediaAsset a = asset(5L, LOGO);
            a.setTitle(OLD_TITLE);
            when(assets.findByIdAndStoreMerchantId(5L, STORE_ID)).thenReturn(Optional.of(a));
            when(assets.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

            service.patch(ContentFixtures.STORE, 5L, new PersistableMediaAsset());

            assertThat(a.getTitle()).isEqualTo(OLD_TITLE);
            assertThat(a.getFolderId()).isNull();
        }

        @Test
        void patchWritesTheMetadataItWasGiven() throws Exception {
            MediaAsset a = asset(5L, LOGO);
            when(assets.findByIdAndStoreMerchantId(5L, STORE_ID)).thenReturn(Optional.of(a));
            when(folders.findByIdAndStoreMerchantId(2L, STORE_ID)).thenReturn(Optional.of(folder(2L, BRAND_KEY)));
            when(assets.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
            PersistableMediaAsset body = new PersistableMediaAsset();
            body.setFolderId(2L);
            body.setTitle("  Logo  ");
            body.setAltTexts(Map.of(EN, ALT_TEXT));
            body.setTags(List.of(BRAND_KEY));

            ReadableMediaAsset out = service.patch(ContentFixtures.STORE, 5L, body);

            assertThat(a.getFolderId()).isEqualTo(2L);
            assertThat(a.getTitle()).isEqualTo("Logo");
            assertThat(out.getAltTexts()).containsEntry(EN, ALT_TEXT);
            assertThat(out.getTags()).containsExactly(BRAND_KEY);
        }

        @Test
        void patchIntoAFolderOfAnotherStoreIsRefused() {
            when(assets.findByIdAndStoreMerchantId(5L, STORE_ID)).thenReturn(Optional.of(asset(5L, LOGO)));
            when(folders.findByIdAndStoreMerchantId(2L, STORE_ID)).thenReturn(Optional.empty());
            PersistableMediaAsset body = new PersistableMediaAsset();
            body.setFolderId(2L);

            assertThatThrownBy(() -> service.patch(ContentFixtures.STORE, 5L, body))
                    .isInstanceOf(ContentNotFoundException.class);
        }

        @Test
        void deletingAReferencedAssetIsAConflictUnlessForced() {
            MediaUsageRow row = new MediaUsageRow();
            row.setAssetId(5L);
            row.setOwnerKind(MediaOwnerKind.CONTENT);
            row.setOwnerRef(REF_TWO);
            row.setContentId(2L);
            when(assets.findByIdAndStoreMerchantId(5L, STORE_ID)).thenReturn(Optional.of(asset(5L, LOGO)));
            when(usage.findByAssetId(5L)).thenReturn(List.of(row));
            when(contents.findById(2L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(ContentFixtures.STORE, 5L, false))
                    .isInstanceOf(ContentConflictException.class);
        }

        @Test
        void aForcedDeleteRemovesTheObjectAndGivesTheBytesBack() throws Exception {
            MediaQuota quota = new MediaQuota();
            quota.setStoreMerchantId(STORE_ID);
            quota.setBytesUsed(500);
            quota.setFileCount(3);
            MediaAsset a = asset(5L, LOGO);
            when(assets.findByIdAndStoreMerchantId(5L, STORE_ID)).thenReturn(Optional.of(a));
            when(usage.findByAssetId(5L)).thenReturn(List.of());
            when(quotas.findById(STORE_ID)).thenReturn(Optional.of(quota));

            service.delete(ContentFixtures.STORE, 5L, true);

            verify(usage).deleteByAssetId(5L);
            verify(storage).delete(a.getStorageKey());
            verify(assets).delete(a);
            assertThat(quota.getBytesUsed()).isEqualTo(400);
            assertThat(quota.getFileCount()).isEqualTo(2);
        }

        @Test
        void theQuotaNeverGoesNegative() throws Exception {
            MediaQuota quota = new MediaQuota();
            quota.setStoreMerchantId(STORE_ID);
            when(assets.findByIdAndStoreMerchantId(5L, STORE_ID)).thenReturn(Optional.of(asset(5L, LOGO)));
            when(usage.findByAssetId(5L)).thenReturn(List.of());
            when(quotas.findById(STORE_ID)).thenReturn(Optional.of(quota));

            service.delete(ContentFixtures.STORE, 5L, false);

            assertThat(quota.getBytesUsed()).isZero();
            assertThat(quota.getFileCount()).isZero();
        }

    }

    @Nested
    class Folders {

        @Test
        void aStoreWithNoFoldersGetsTheStarterSet() {
            when(folders.findByStoreMerchantIdAndKey(eq(STORE_ID), any())).thenReturn(Optional.empty());
            when(folders.findByStoreMerchantIdOrderByPositionAscIdAsc(STORE_ID))
                    .thenReturn(List.of(folder(1L, BANNERS_KEY)));
            when(assets.countByStoreMerchantIdAndFolderId(STORE_ID, 1L)).thenReturn(4L);

            assertThat(service.folders(ContentFixtures.STORE)).singleElement()
                    .satisfies(f -> assertThat(f.getFileCount()).isEqualTo(4L));
            verify(folders, org.mockito.Mockito.times(5)).save(any());
        }

        @Test
        void aCompleteStarterSetIsNotSeededAgain() {
            when(folders.findByStoreMerchantIdAndKey(eq(STORE_ID), any()))
                    .thenReturn(Optional.of(folder(1L, BANNERS_KEY)));
            when(folders.findByStoreMerchantIdOrderByPositionAscIdAsc(STORE_ID))
                    .thenReturn(List.of(folder(1L, BANNERS_KEY)));

            service.folders(ContentFixtures.STORE);

            verify(folders, never()).save(any());
        }

        /**
         * The starter set used to be skipped whenever the store had any folder at all, so a seller who made one
         * of their own before opening the library never got the defaults.
         */
        @Test
        void aSellersOwnFolderDoesNotSuppressTheStarterSet() {
            when(folders.findByStoreMerchantIdAndKey(eq(STORE_ID), any())).thenReturn(Optional.empty());
            when(folders.findByStoreMerchantIdOrderByPositionAscIdAsc(STORE_ID))
                    .thenReturn(List.of(folder(9L, "campaign")));

            service.folders(ContentFixtures.STORE);

            verify(folders, org.mockito.Mockito.times(5)).save(any());
        }

        @Test
        void aFolderWithoutAKeyIsSluggedFromItsName() {
            when(folders.findByStoreMerchantIdAndKey(STORE_ID, BRAND_ASSETS_KEY)).thenReturn(Optional.empty());
            when(folders.saveAndFlush(any())).thenAnswer(i -> {
                MediaFolder f = i.getArgument(0);
                f.setId(7L);
                return f;
            });
            var body = new com.asrevo.cvhome.content.model.media.MediaFolder();
            body.setName("  Brand Assets  ");

            var out = service.createFolder(ContentFixtures.STORE, body);

            assertThat(out.getKey()).isEqualTo(BRAND_ASSETS_KEY);
            assertThat(out.getName()).isEqualTo("Brand Assets");
            assertThat(out.getPosition()).isEqualTo(100);
        }

        @Test
        void creatingAFolderThatAlreadyExistsUpdatesItInPlace() {
            MediaFolder existing = folder(7L, BRAND_KEY);
            when(folders.findByStoreMerchantIdAndKey(STORE_ID, BRAND_KEY)).thenReturn(Optional.of(existing));
            when(folders.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
            var body = new com.asrevo.cvhome.content.model.media.MediaFolder();
            body.setKey(BRAND_KEY);
            body.setName(BRAND_NAME);
            body.setPosition(2);

            assertThat(service.createFolder(ContentFixtures.STORE, body).getId()).isEqualTo(7L);
            assertThat(existing.getPosition()).isEqualTo(2);
        }

        @Test
        void renamingAFolderOfAnotherStoreIsRefused() {
            when(folders.findByIdAndStoreMerchantId(7L, STORE_ID)).thenReturn(Optional.empty());
            var body = new com.asrevo.cvhome.content.model.media.MediaFolder();
            body.setName(BRAND_NAME);

            assertThatThrownBy(() -> service.renameFolder(ContentFixtures.STORE, 7L, body))
                    .isInstanceOf(ContentNotFoundException.class);
        }

        @Test
        void renamingKeepsThePositionWhenNoneIsSent() throws Exception {
            MediaFolder existing = folder(7L, BRAND_KEY);
            when(folders.findByIdAndStoreMerchantId(7L, STORE_ID)).thenReturn(Optional.of(existing));
            when(folders.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
            var body = new com.asrevo.cvhome.content.model.media.MediaFolder();
            body.setName("  Brand  ");

            assertThat(service.renameFolder(ContentFixtures.STORE, 7L, body).getName()).isEqualTo(BRAND_NAME);
            assertThat(existing.getPosition()).isEqualTo(1);
        }

        @Test
        void deletingANonEmptyFolderWithoutATargetIsAConflict() {
            when(folders.findByIdAndStoreMerchantId(7L, STORE_ID)).thenReturn(Optional.of(folder(7L, BRAND_KEY)));
            when(assets.countByStoreMerchantIdAndFolderId(STORE_ID, 7L)).thenReturn(3L);

            assertThatThrownBy(() -> service.deleteFolder(ContentFixtures.STORE, 7L, null))
                    .isInstanceOf(ContentConflictException.class)
                    .hasMessageContaining("still holds");
        }

        @Test
        void deletingANonEmptyFolderMovesItsFilesFirst() throws Exception {
            MediaFolder from = folder(7L, BRAND_KEY);
            when(folders.findByIdAndStoreMerchantId(7L, STORE_ID)).thenReturn(Optional.of(from));
            when(folders.findByIdAndStoreMerchantId(8L, STORE_ID)).thenReturn(Optional.of(folder(8L, "docs")));
            when(assets.countByStoreMerchantIdAndFolderId(STORE_ID, 7L)).thenReturn(3L);

            service.deleteFolder(ContentFixtures.STORE, 7L, 8L);

            verify(assets).moveFolder(STORE_ID, 7L, 8L);
            verify(folders).delete(from);
        }

        @Test
        void movingIntoAFolderOfAnotherStoreIsRefused() {
            when(folders.findByIdAndStoreMerchantId(7L, STORE_ID)).thenReturn(Optional.of(folder(7L, BRAND_KEY)));
            when(folders.findByIdAndStoreMerchantId(8L, STORE_ID)).thenReturn(Optional.empty());
            when(assets.countByStoreMerchantIdAndFolderId(STORE_ID, 7L)).thenReturn(3L);

            assertThatThrownBy(() -> service.deleteFolder(ContentFixtures.STORE, 7L, 8L))
                    .isInstanceOf(ContentNotFoundException.class);
            verify(assets, never()).moveFolder(anyString(), anyLong(), anyLong());
        }

        @Test
        void anEmptyFolderIsDeletedOutright() throws Exception {
            MediaFolder from = folder(7L, BRAND_KEY);
            when(folders.findByIdAndStoreMerchantId(7L, STORE_ID)).thenReturn(Optional.of(from));
            when(assets.countByStoreMerchantIdAndFolderId(STORE_ID, 7L)).thenReturn(0L);

            service.deleteFolder(ContentFixtures.STORE, 7L, null);

            verify(assets, never()).moveFolder(anyString(), anyLong(), anyLong());
            verify(folders).delete(from);
        }

    }

    @Nested
    class Summary {

        @Test
        void aStoreWithoutAQuotaRowContributesNothing() {
            when(quotas.findById(STORE_ID)).thenReturn(Optional.empty());
            ContentSummary summary = new ContentSummary();
            Map<String, Long> counts = new java.util.LinkedHashMap<>();

            service.contribute(ContentFixtures.STORE, summary, counts);

            assertThat(counts).isEmpty();
        }

        @Test
        void theQuotaRowFillsTheMediaCard() {
            MediaQuota quota = new MediaQuota();
            quota.setBytesUsed(900);
            quota.setFileCount(3);
            when(quotas.findById(STORE_ID)).thenReturn(Optional.of(quota));
            ContentSummary summary = new ContentSummary();
            Map<String, Long> counts = new java.util.LinkedHashMap<>();

            service.contribute(ContentFixtures.STORE, summary, counts);

            assertThat(summary.getMedia().getBytesUsed()).isEqualTo(900);
            assertThat(counts).containsEntry("media", 3L);
        }

    }

}
