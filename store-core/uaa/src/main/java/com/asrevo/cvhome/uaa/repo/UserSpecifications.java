package com.asrevo.cvhome.uaa.repo;

import org.springframework.data.jpa.domain.Specification;

import com.asrevo.cvhome.uaa.domain.User;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> hasMetadataField(String key, String value) {
        return (root, query, cb) -> cb
                .equal(cb.function("jsonb_extract_path_text", String.class, root.get("metadata"), cb.literal(key)), value);
    }

}
