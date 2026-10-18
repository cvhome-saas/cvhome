package com.asrevo.cvhome.cache;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.asrevo.cvhome.commons.domain.KeyPart;

/**
 * A page request as a key part: {@code p<page>s<size>:<sort>}. Its own class so the key model never loads
 * Spring Data's types unless a caller passes one.
 */
public final class PageableKeys {

    private PageableKeys() {
    }

    public static KeyPart of(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return new Page("all");
        }
        StringBuilder text = new StringBuilder("p").append(pageable.getPageNumber()).append('s')
                .append(pageable.getPageSize());
        if (pageable.getSort().isSorted()) {
            text.append(':');
            for (Sort.Order order : pageable.getSort()) {
                text.append(order.getProperty()).append(order.isAscending() ? '+' : '-');
            }
        }
        return new Page(text.toString());
    }

    private record Page(String text) implements KeyPart {

        @Override
        public String cacheKeyPart() {
            return text;
        }
    }
}
