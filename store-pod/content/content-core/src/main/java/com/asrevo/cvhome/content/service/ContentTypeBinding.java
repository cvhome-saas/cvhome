package com.asrevo.cvhome.content.service;

import java.util.List;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.ContentDescription;
import com.asrevo.cvhome.content.errors.ContentConflictException;
import com.asrevo.cvhome.content.errors.ContentRuleException;
import com.asrevo.cvhome.content.model.common.PersistableContent;
import com.asrevo.cvhome.errors.FieldError;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

/**
 * What differs between the content types — the type-specific columns, the row subtitle and the extra publish rules.
 * {@link ContentItemService} does everything else once. One binding bean per {@link ContentType}.
 *
 * @param <P> the persistable DTO of the type
 * @param <R> its readable DTO (extends the persistable one with the read-only quartet)
 */
public interface ContentTypeBinding<P extends PersistableContent, R extends P> {

    ContentType type();

    Class<P> persistableClass();

    R newReadable();

    /**
     * Copies the type-specific fields of {@code dto} onto {@code entity}. Common fields are already applied.
     */
    void apply(Content entity, P dto) throws ContentRuleException, ContentConflictException;

    /**
     * Fills the type-specific fields of {@code dto} from {@code entity}. Common fields are already populated.
     */
    void populate(Content entity, R dto);

    /**
     * The second line of the console row, or {@code null} to fall back to the slug.
     */
    default String subtitle(Content entity, LanguageCode language) {
        return null;
    }

    /**
     * Whether a complete translation needs a body as well as a title (pages, posts, FAQ, policies do; banners
     * do not).
     */
    default boolean requiresBody() {
        return true;
    }

    /**
     * Type-specific publish rules beyond "the source locale is complete". {@code source} is that locale's row.
     */
    default List<FieldError> publishProblems(Content entity, ContentDescription source) {
        return List.of();
    }

    /**
     * Called before a hard delete; throw to refuse unless {@code force}.
     */
    default void beforeDelete(Content entity, boolean force) throws ContentConflictException {
    }

    /**
     * Called after every persist of the row (create, update, restore, transition) inside the same transaction.
     */
    default void afterSave(Content entity) {
    }

    default void afterDelete(Content entity) {
    }

    /**
     * The storefront path of the item ({@code /content/<slug>}, {@code /blog/<slug>}), or {@code null} when the type
     * has no page of its own. A slug change of a published item writes a redirect from the old path to the new.
     */
    default String storefrontPath(Content entity) {
        return null;
    }

}
