package com.asrevo.cvhome.content.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.content.Content;
import com.asrevo.cvhome.content.entity.post.ContentPost;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.model.ContentView;
import com.asrevo.cvhome.content.model.post.PostView;
import com.asrevo.cvhome.content.model.post.PostWriteRequest;
import com.asrevo.cvhome.content.repository.ContentPostRepository;
import com.asrevo.cvhome.content.repository.ContentRepository;

@Service
public class PostService {
    private static final int WORDS_PER_MINUTE = 220;

    private final ContentV2Service contentService;
    private final ContentRepository contentRepository;
    private final ContentPostRepository postRepository;

    public PostService(ContentV2Service contentService, ContentRepository contentRepository,
                       ContentPostRepository postRepository) {
        this.contentService = contentService;
        this.contentRepository = contentRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public PostView create(StoreMerchantId store, LanguageCode language, PostWriteRequest request, String actor)
            throws ContentNotFoundException {
        ContentView created = contentService.create(store, language, request.content(), actor);
        Content content = contentRepository.findByIdAndStoreMerchantId(created.id(), store)
                .orElseThrow(() -> ContentNotFoundException.forId(created.id()));
        ContentPost post = new ContentPost();
        post.setContent(content);
        post.setExcerpt(request.excerpt());
        post.setHeroMediaId(request.heroMediaId());
        post.setAuthorSnapshot(request.author());
        post.setReadingMinutes(readingMinutes(request.content().description()));
        post.setFeatured(request.featured());
        return toView(postRepository.save(post), created);
    }

    @Transactional(readOnly = true)
    public PostView find(StoreMerchantId store, Long id) throws ContentNotFoundException {
        ContentPost post = postRepository.findByIdAndContentStoreMerchantId(id, store)
                .orElseThrow(() -> ContentNotFoundException.forId(id));
        return toView(post, contentService.find(store, id));
    }

    private static int readingMinutes(String body) {
        if (body == null || body.isBlank()) {
            return 1;
        }
        int words = body.strip().split("\\s+").length;
        return Math.max(1, (words + WORDS_PER_MINUTE - 1) / WORDS_PER_MINUTE);
    }

    private static PostView toView(ContentPost post, ContentView content) {
        return new PostView(content, post.getExcerpt(), post.getHeroMediaId(), post.getAuthorSnapshot(),
                post.getReadingMinutes(), post.isFeatured());
    }
}
