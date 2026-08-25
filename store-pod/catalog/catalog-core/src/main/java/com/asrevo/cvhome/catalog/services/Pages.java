package com.asrevo.cvhome.catalog.services;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

/**
 * Turns a Spring page of entities into the wire envelope every list endpoint answers with.
 */
public final class Pages {

    private Pages() {
    }

    public static <E, R extends Serializable> ReadableEntityList<R> toReadable(Page<E> page, Function<E, R> convert) {
        return of(page.getContent().stream().map(convert).toList(), page.getTotalElements(), page.getTotalPages(),
                page.getNumber());
    }

    public static <R extends Serializable> ReadableEntityList<R> of(List<R> content, long totalElements,
                                                                    int totalPages, int pageNumber) {
        ReadableEntityList<R> list = new ReadableEntityList<>();
        list.setContent(content);
        list.setSize(content.size());
        list.setTotalElements(totalElements);
        list.setTotalPages(totalPages);
        list.setPageNumber(pageNumber);
        return list;
    }

    public static <R extends Serializable> ReadableEntityList<R> single(List<R> content) {
        return of(content, content.size(), 1, 0);
    }
}
