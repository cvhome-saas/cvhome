package com.asrevo.cvhome.content.service;

import java.util.List;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.content.Content;
import com.asrevo.cvhome.content.entity.page.ContentPage;
import com.asrevo.cvhome.content.entity.page.PageBlock;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.model.ContentView;
import com.asrevo.cvhome.content.model.page.PageBlockSpec;
import com.asrevo.cvhome.content.model.page.PageView;
import com.asrevo.cvhome.content.model.page.PageWriteRequest;
import com.asrevo.cvhome.content.repository.ContentPageRepository;
import com.asrevo.cvhome.content.repository.ContentRepository;

import tools.jackson.databind.ObjectMapper;

@Service
public class PageService {
    private static final String ANCHOR = "a";
    private static final PolicyFactory HTML_POLICY = new HtmlPolicyBuilder()
            .allowElements("p", "br", "strong", "em", "ul", "ol", "li", "blockquote", "h2", "h3", "h4",
                    ANCHOR, "span", "div")
            .allowAttributes("href", "title").onElements(ANCHOR)
            .allowUrlProtocols("https", "http", "mailto")
            .toFactory();

    private final ContentV2Service contentService;
    private final ContentRepository contentRepository;
    private final ContentPageRepository pageRepository;
    private final ObjectMapper objectMapper;

    public PageService(ContentV2Service contentService, ContentRepository contentRepository,
                       ContentPageRepository pageRepository, ObjectMapper objectMapper) {
        this.contentService = contentService;
        this.contentRepository = contentRepository;
        this.pageRepository = pageRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PageView create(StoreMerchantId store, LanguageCode language, PageWriteRequest request, String actor)
            throws ContentNotFoundException {
        ContentView created = contentService.create(store, language, request.content(), actor);
        Content content = contentRepository.findByIdAndStoreMerchantId(created.id(), store)
                .orElseThrow(() -> ContentNotFoundException.forId(created.id()));
        ContentPage page = new ContentPage();
        page.setContent(content);
        page.setTemplate(request.template());
        page.setShowInSitemap(request.showInSitemap());
        page.setParentPageId(request.parentPageId());
        int position = 0;
        for (PageBlockSpec spec : request.blocks()) {
            PageBlock block = new PageBlock();
            block.setPosition(position++);
            block.setBlockType(blockType(spec));
            block.setPayload(objectMapper.writeValueAsString(sanitize(spec)));
            page.addBlock(block);
        }
        return toView(pageRepository.save(page), created, language, language, false);
    }

    @Transactional(readOnly = true)
    public PageView find(StoreMerchantId store, Long id) throws ContentNotFoundException {
        return find(store, id, null, null, false);
    }

    @Transactional(readOnly = true)
    public PageView find(StoreMerchantId store, Long id, LanguageCode requestedLanguage,
                         LanguageCode resolvedLanguage, boolean fallback) throws ContentNotFoundException {
        ContentPage page = pageRepository.findByIdAndContentStoreMerchantId(id, store)
                .orElseThrow(() -> ContentNotFoundException.forId(id));
        return toView(page, contentService.find(store, id), requestedLanguage, resolvedLanguage, fallback);
    }

    private PageView toView(ContentPage page, ContentView content, LanguageCode requestedLanguage,
                            LanguageCode resolvedLanguage, boolean fallback) {
        List<PageBlockSpec> blocks = page.getBlocks().stream()
                .map(it -> objectMapper.readValue(it.getPayload(), PageBlockSpec.class))
                .toList();
        return new PageView(content, page.getTemplate(), page.isShowInSitemap(), page.getParentPageId(), blocks,
                requestedLanguage, resolvedLanguage, fallback);
    }

    private static PageBlockSpec sanitize(PageBlockSpec spec) {
        if (spec instanceof PageBlockSpec.RichText richText) {
            return new PageBlockSpec.RichText(HTML_POLICY.sanitize(richText.html()));
        }
        if (spec instanceof PageBlockSpec.HtmlEmbed htmlEmbed) {
            return new PageBlockSpec.HtmlEmbed(HTML_POLICY.sanitize(htmlEmbed.html()));
        }
        return spec;
    }

    private static String blockType(PageBlockSpec spec) {
        return switch (spec) {
            case PageBlockSpec.RichText ignored -> "RICH_TEXT";
            case PageBlockSpec.Image ignored -> "IMAGE";
            case PageBlockSpec.Gallery ignored -> "GALLERY";
            case PageBlockSpec.VideoLink ignored -> "VIDEO_LINK";
            case PageBlockSpec.ProductGrid ignored -> "PRODUCT_GRID";
            case PageBlockSpec.Reference reference -> "%s_REFERENCE".formatted(
                    reference.referenceType().toUpperCase());
            case PageBlockSpec.HtmlEmbed ignored -> "HTML_EMBED";
            case PageBlockSpec.Spacer ignored -> "SPACER";
            case PageBlockSpec.Cta ignored -> "CTA";
        };
    }
}
