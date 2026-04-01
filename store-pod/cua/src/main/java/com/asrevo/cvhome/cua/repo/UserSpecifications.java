package com.asrevo.cvhome.cua.repo;

import com.asrevo.cvhome.cua.domain.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

	public static Specification<User> hasMetadataField(String key, String value) {
		return (root, query, cb) -> cb
			.equal(cb.function("jsonb_extract_path_text", String.class, root.get("metadata"), cb.literal(key)), value);
	}

}
