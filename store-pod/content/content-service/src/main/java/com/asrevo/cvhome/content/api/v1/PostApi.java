package com.asrevo.cvhome.content.api.v1;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.v1.support.ContentPermissions;
import com.asrevo.cvhome.content.errors.ContentConflictException;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.model.post.PersistablePost;
import com.asrevo.cvhome.content.model.post.PostCategory;
import com.asrevo.cvhome.content.model.post.ReadablePost;
import com.asrevo.cvhome.content.service.ContentItemService;
import com.asrevo.cvhome.content.service.PostCategoryService;
import com.asrevo.cvhome.content.service.binding.PostBinding;

/**
 * Blog posts plus their categories ({@code /post-categories} lives here because it has no other home).
 */
@RestController
@RequestMapping("/api/v1/private/content/posts")
public class PostApi extends WorkflowContentApi<PersistablePost, ReadablePost> {

    private final PostCategoryService categories;

    public PostApi(ContentItemService items, PostBinding binding, PostCategoryService categories) {
        super(items, binding);
        this.categories = categories;
    }

    @GetMapping("categories")
    @PreAuthorize(ContentPermissions.READ)
    public List<PostCategory> categories(StoreMerchantId merchantStore, LanguageCode language) {
        return categories.list(merchantStore);
    }

    @PostMapping("categories")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ContentPermissions.MANAGE)
    public PostCategory createCategory(StoreMerchantId merchantStore, LanguageCode language,
                                       @RequestBody @Valid PostCategory body) throws ContentConflictException {
        return categories.create(merchantStore, body);
    }

    @PutMapping("categories/{id}")
    @PreAuthorize(ContentPermissions.MANAGE)
    public PostCategory updateCategory(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id,
                                       @RequestBody @Valid PostCategory body)
            throws ContentNotFoundException, ContentConflictException {
        return categories.update(merchantStore, id, body);
    }

    @DeleteMapping("categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(ContentPermissions.MANAGE)
    public void deleteCategory(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id)
            throws ContentNotFoundException {
        categories.delete(merchantStore, id);
    }

}
