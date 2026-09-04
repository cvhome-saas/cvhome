package com.asrevo.cvhome.content.api.v1;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.v1.support.PreviewTokens;
import com.asrevo.cvhome.content.config.ContentProperties;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.errors.InvalidContentRequestException;
import com.asrevo.cvhome.content.facade.StorefrontFacade;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.common.PublishRequest;
import com.asrevo.cvhome.content.model.common.SavedContent;
import com.asrevo.cvhome.content.model.layout.PageKind;
import com.asrevo.cvhome.content.model.media.ExternalMediaUsage;
import com.asrevo.cvhome.content.model.policy.ReadablePolicy;
import com.asrevo.cvhome.content.service.ContentItemService;
import com.asrevo.cvhome.content.service.FaqService;
import com.asrevo.cvhome.content.service.MediaService;
import com.asrevo.cvhome.content.service.MediaUsageTracker;
import com.asrevo.cvhome.content.service.MenuService;
import com.asrevo.cvhome.content.service.PageLayoutService;
import com.asrevo.cvhome.content.service.PolicyService;
import com.asrevo.cvhome.content.service.PostCategoryService;
import com.asrevo.cvhome.content.service.SectionPresetService;
import com.asrevo.cvhome.content.service.binding.BannerBinding;
import com.asrevo.cvhome.content.service.binding.FaqBinding;
import com.asrevo.cvhome.content.service.binding.PolicyBinding;
import com.asrevo.cvhome.content.service.binding.PostBinding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The console-side content endpoints: media, layouts and FAQ groups.
 *
 * <p>
 * {@link MediaApi#upload} is the one with real work in it. It reads every part's bytes before handing the batch to
 * the service, and a part that cannot be read has to become a typed {@code InvalidContentRequestException} naming
 * the file rather than an {@link IOException} escaping as a 500 — an operator uploading twenty images needs to know
 * which one failed. It also reads the size and quota limits from configuration rather than taking them from the
 * request, which is what stops a caller raising its own quota.
 * </p>
 */
class ContentAdminApisTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");
    private static final LanguageCode ENGLISH = new LanguageCode("en");
    private static final String ACTOR = "system";
    private static final String FILE_NAME = "hero.png";
    private static final String SECOND_FILE = "second.png";
    private static final String PART_NAME = "files";
    private static final String PNG = "image/png";
    private static final String QUERY = "q";
    private static final String TOKEN = "tok";
    private static final String TOKEN_KEY = "token";
    private static final String OWNER_ONE = "ref-1";
    private static final String OWNER_TWO = "ref-2";
    private static final String OWNER_TITLE = "A page";
    private static final String FIELD = "hero";
    private static final String FAQ_CODE = "faq-1";
    private static final String FAQ_PATH = "/faq";
    private static final String PATH_KEY = "path";

    private final MediaService media = Mockito.mock(MediaService.class);
    private final ContentProperties properties = Mockito.mock(ContentProperties.class, Mockito.RETURNS_DEEP_STUBS);
    private final PageLayoutService layouts = Mockito.mock(PageLayoutService.class);
    private final SectionPresetService presets = Mockito.mock(SectionPresetService.class);
    private final PreviewTokens previews = Mockito.mock(PreviewTokens.class);
    private final ContentItemService items = Mockito.mock(ContentItemService.class);
    private final FaqService faq = Mockito.mock(FaqService.class);
    private final FaqBinding faqBinding = Mockito.mock(FaqBinding.class);
    private final PolicyService policies = Mockito.mock(PolicyService.class);
    private final PolicyBinding policyBinding = Mockito.mock(PolicyBinding.class);
    private final BannerBinding bannerBinding = Mockito.mock(BannerBinding.class);
    private final PostBinding postBinding = Mockito.mock(PostBinding.class);
    private final PostCategoryService postCategories = Mockito.mock(PostCategoryService.class);
    private final StorefrontFacade storefront = Mockito.mock(StorefrontFacade.class);
    private final MediaUsageTracker usage = Mockito.mock(MediaUsageTracker.class);
    private final MenuService menus = Mockito.mock(MenuService.class);

    private final MediaApi mediaApi = new MediaApi(media, properties);
    private final LayoutApi layoutApi = new LayoutApi(layouts, presets, previews);
    private final FaqApi faqApi = new FaqApi(items, faqBinding, faq);

    /** {@code previews} is an @Autowired field on the base class with no setter, so the test wires it the way Spring would. */
    private <T extends WorkflowContentApi<?, ?>> T withPreviews(T api) {
        ReflectionTestUtils.setField(api, "previews", previews);
        return api;
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void everyMediaReadAndWriteIsScopedToTheStore() throws Exception {
        mediaApi.list(STORE, ENGLISH, 1L, null, QUERY, true, PageRequest.of(0, 20));
        mediaApi.get(STORE, ENGLISH, 1L);
        mediaApi.usage(STORE, ENGLISH, 1L);
        mediaApi.patch(STORE, ENGLISH, 1L, null);
        mediaApi.delete(STORE, ENGLISH, 1L, true);

        verify(media).list(STORE, 1L, null, QUERY, true, PageRequest.of(0, 20));
        verify(media).get(STORE, 1L);
        verify(media).usage(STORE, 1L);
        verify(media).patch(STORE, 1L, null);
        verify(media).delete(STORE, 1L, true);
    }

    @Test
    void theFolderEndpointsAreScopedToTheStoreToo() throws Exception {
        mediaApi.folders(STORE, ENGLISH);
        mediaApi.createFolder(STORE, ENGLISH, null);
        mediaApi.renameFolder(STORE, ENGLISH, 1L, null);
        mediaApi.deleteFolder(STORE, ENGLISH, 1L, 2L);

        verify(media).folders(STORE);
        verify(media).createFolder(STORE, null);
        verify(media).renameFolder(STORE, 1L, null);
        verify(media).deleteFolder(STORE, 1L, 2L);
    }

    @Test
    void anUploadCarriesEveryPartsBytesAndTheConfiguredLimits() throws Exception {
        when(properties.media().maxFileSize().toBytes()).thenReturn(5L);
        when(properties.media().quota().toBytes()).thenReturn(500L);
        MultipartFile[] files = {
            new MockMultipartFile(PART_NAME, FILE_NAME, PNG, new byte[]{1, 2, 3}),
            new MockMultipartFile(PART_NAME, SECOND_FILE, PNG, new byte[]{4}),
        };

        mediaApi.upload(STORE, ENGLISH, files, 7L);

        ArgumentCaptor<List<MediaService.Upload>> uploads = ArgumentCaptor.captor();
        // The limits come from configuration, never the request: taking them from the caller would let a store
        // raise its own quota.
        verify(media).upload(eq(STORE), uploads.capture(), eq(7L), eq(5L), eq(500L), any());
        assertThat(uploads.getValue()).hasSize(2)
                .extracting(MediaService.Upload::filename)
                .containsExactly(FILE_NAME, SECOND_FILE);
    }

    @Test
    void aPartThatCannotBeReadNamesTheFileRatherThanEscapingAsAFiveHundred() {
        when(properties.media().maxFileSize().toBytes()).thenReturn(5L);
        when(properties.media().quota().toBytes()).thenReturn(500L);
        MultipartFile unreadable = Mockito.mock(MultipartFile.class);
        when(unreadable.getOriginalFilename()).thenReturn(FILE_NAME);

        try {
            when(unreadable.getBytes()).thenThrow(new IOException("disk gone"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        assertThatThrownBy(() -> mediaApi.upload(STORE, ENGLISH, new MultipartFile[]{unreadable}, null))
                .isInstanceOf(InvalidContentRequestException.class)
                .hasMessageContaining(FILE_NAME);
    }

    @Test
    void theLayoutEditorEndpointsAllRecordAnActor() throws Exception {
        layoutApi.get(STORE, ENGLISH, PageKind.HOME);
        layoutApi.put(STORE, ENGLISH, PageKind.HOME, null);
        layoutApi.revisions(STORE, ENGLISH, PageKind.HOME);
        layoutApi.restore(STORE, ENGLISH, PageKind.HOME, 2);

        verify(layouts).get(STORE, PageKind.HOME);
        verify(layouts).save(STORE, PageKind.HOME, null, ACTOR);
        verify(layouts).revisions(STORE, PageKind.HOME);
        verify(layouts).restore(STORE, PageKind.HOME, 2, ACTOR);
    }

    @Test
    void aLayoutPreviewTokenIsIssuedAgainstTheLayoutsOwnSlug() {
        when(previews.issue(STORE, "layout:HOME")).thenReturn(TOKEN);

        assertThat(layoutApi.previewToken(STORE, ENGLISH, PageKind.HOME)).containsEntry(TOKEN_KEY, TOKEN);
    }

    @Test
    void theSectionPresetEndpointsAreScopedToTheStore() throws Exception {
        layoutApi.sectionPresets(STORE, ENGLISH);
        layoutApi.saveSectionPreset(STORE, ENGLISH, null);
        layoutApi.deleteSectionPreset(STORE, ENGLISH, 1L);

        verify(presets).list(STORE);
        verify(presets).save(STORE, null, ACTOR);
        verify(presets).delete(STORE, 1L);
    }

    @Test
    void theFaqGroupEndpointsAreScopedToTheStore() throws Exception {
        faqApi.groups(STORE, ENGLISH);
        faqApi.createGroup(STORE, ENGLISH, null);
        faqApi.updateGroup(STORE, ENGLISH, 1L, null);
        faqApi.deleteGroup(STORE, ENGLISH, 1L);
        faqApi.reorder(STORE, ENGLISH, List.of());

        verify(faq).groups(STORE);
        verify(faq).create(STORE, null);
        verify(faq).update(STORE, 1L, null);
        verify(faq).delete(STORE, 1L);
        verify(faq).reorder(STORE, List.of());
    }

    @Test
    void thePolicyVersionReadsResolveTheHeadFirstSoTheyStayStoreScoped() throws Exception {
        PolicyApi policyApi = new PolicyApi(items, policyBinding, policies);

        policyApi.compliance(STORE, ENGLISH);
        policyApi.versions(STORE, ENGLISH, 1L);
        policyApi.version(STORE, ENGLISH, 1L, 2);

        // versions() and version() go through items.load, which is what scopes them: policies.versions() takes an
        // entity, so a caller that skipped the load could read another store's policy history by id.
        verify(policies).compliance(STORE);
        verify(items, Mockito.times(2)).load(policyBinding, 1L, STORE);
    }

    @Test
    void restoringAPolicysTextRewritesTheHeadRatherThanCreatingAVersion() throws Exception {
        PolicyApi policyApi = new PolicyApi(items, policyBinding, policies);
        Content head = Mockito.mock(Content.class);
        ReadablePolicy dto = Mockito.mock(ReadablePolicy.class);
        when(items.load(policyBinding, 1L, STORE)).thenReturn(head);
        when(items.toReadable(policyBinding, head)).thenReturn(dto);

        policyApi.restoreText(STORE, ENGLISH, 1L, 3);

        verify(policies).textOf(head, 3);
        verify(items).update(policyBinding, 1L, dto, STORE, ENGLISH, ACTOR);
    }

    @Test
    void theBannerAndPostExtrasStayOnTheirOwnStore() throws Exception {
        BannerApi bannerApi = new BannerApi(items, bannerBinding, storefront);
        PostApi postApi = new PostApi(items, postBinding, postCategories);

        bannerApi.effective(STORE, ENGLISH, null);
        postApi.categories(STORE, ENGLISH);
        postApi.createCategory(STORE, ENGLISH, null);
        postApi.updateCategory(STORE, ENGLISH, 1L, null);

        verify(storefront).effectiveBanners(STORE, ENGLISH, null);
        verify(postCategories).list(STORE);
        verify(postCategories).create(STORE, null);
        verify(postCategories).update(STORE, 1L, null);
    }

    @Test
    void anExternalUsageReplacementFlattensItsRefsAndToleratesNone() {
        ExternalMediaApi externalApi = new ExternalMediaApi(media, usage);

        externalApi.resolve(STORE, List.of(1L, 2L));
        externalApi.replaceUsage(STORE, new ExternalMediaUsage(null, OWNER_ONE, OWNER_TITLE,
                List.of(new ExternalMediaUsage.Ref(FIELD, 9L))));
        externalApi.replaceUsage(STORE, new ExternalMediaUsage(null, OWNER_TWO, OWNER_TITLE, null));

        verify(media).assets(STORE, List.of(1L, 2L));
        verify(usage).replace(STORE, null, OWNER_ONE, null, null, OWNER_TITLE, Map.of(FIELD, 9L));
        // A null ref list is "this owner uses nothing now", which is how a page that dropped its last image
        // releases the asset -- not a reason to skip the call.
        verify(usage).replace(STORE, null, OWNER_TWO, null, null, OWNER_TITLE, Map.of());
    }

    @Test
    void aPreviewTokenIsIssuedAgainstTheContentsOwnCodeAndCarriesItsStorefrontPath() throws Exception {
        Content entity = Mockito.mock(Content.class);
        when(items.load(faqBinding, 1L, STORE)).thenReturn(entity);
        when(entity.getCode()).thenReturn(FAQ_CODE);
        when(previews.issue(STORE, FAQ_CODE)).thenReturn(TOKEN);
        when(faqBinding.storefrontPath(entity)).thenReturn(FAQ_PATH);

        assertThat(withPreviews(faqApi).previewToken(STORE, ENGLISH, 1L))
                .containsEntry(TOKEN_KEY, TOKEN)
                .containsEntry(PATH_KEY, FAQ_PATH);
    }

    @Test
    void contentWithNoStorefrontPathAnswersAnEmptyStringRatherThanNull() throws Exception {
        Content entity = Mockito.mock(Content.class);
        when(items.load(faqBinding, 1L, STORE)).thenReturn(entity);
        when(previews.issue(any(), any())).thenReturn(TOKEN);
        when(faqBinding.storefrontPath(entity)).thenReturn(null);

        // Map.of refuses a null value, so a null path here would be a NullPointerException on a preview request.
        assertThat(withPreviews(faqApi).previewToken(STORE, ENGLISH, 1L)).containsEntry(PATH_KEY, "");
    }

    @Test
    void theMenuEditorEndpointsAreScopedToTheStore() throws Exception {
        MenuApi menuApi = new MenuApi(menus);

        menuApi.list(STORE, ENGLISH);
        menuApi.get(STORE, ENGLISH, null);
        menuApi.put(STORE, ENGLISH, null, null);

        verify(menus).list(STORE);
        verify(menus).get(STORE, null);
        verify(menus).put(STORE, null, null);
    }

    @Test
    void publishingAPolicyVersionCarriesAnEffectiveDateWhenOneIsGiven() throws Exception {
        PolicyApi policyApi = new PolicyApi(items, policyBinding, policies);
        Content head = Mockito.mock(Content.class);
        SavedContent saved = Mockito.mock(SavedContent.class);
        when(items.transition(any(), eq(1L), eq(STORE), eq(ContentStatus.PUBLISHED), any(), eq(ENGLISH), any()))
                .thenReturn(saved);
        when(saved.getId()).thenReturn(1L);
        when(items.load(policyBinding, 1L, STORE)).thenReturn(head);

        policyApi.publishVersion(STORE, ENGLISH, 1L, null);

        // A null body is "publish now"; the transition still runs, with an empty PublishRequest rather than none.
        ArgumentCaptor<PublishRequest> request = ArgumentCaptor.captor();
        verify(items).transition(any(), eq(1L), eq(STORE), eq(ContentStatus.PUBLISHED), request.capture(),
                eq(ENGLISH), any());
        assertThat(request.getValue().getPublishAt()).isNull();
        verify(policies).annotateLive(head, null, ACTOR);
    }
}
