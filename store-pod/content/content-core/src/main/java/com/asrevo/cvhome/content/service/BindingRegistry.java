package com.asrevo.cvhome.content.service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.content.model.common.PersistableContent;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

/**
 * All {@link ContentTypeBinding} beans by type.
 */
@Component
public class BindingRegistry {

    private final Map<ContentType, ContentTypeBinding<?, ?>> bindings = new EnumMap<>(ContentType.class);

    public BindingRegistry(List<ContentTypeBinding<?, ?>> all) {
        for (ContentTypeBinding<?, ?> b : all) {
            bindings.put(b.type(), b);
        }
    }

    public Optional<ContentTypeBinding<?, ?>> find(ContentType type) {
        return Optional.ofNullable(bindings.get(type));
    }

    @SuppressWarnings("unchecked")
    public <P extends PersistableContent, R extends P> ContentTypeBinding<P, R> require(ContentType type) {
        return (ContentTypeBinding<P, R>) find(type)
                .orElseThrow(() -> new IllegalArgumentException(String.format("No content binding for %s", type)));
    }

    public List<ContentType> workflowTypes() {
        return bindings.keySet().stream().filter(ContentType::workflow).toList();
    }

}
