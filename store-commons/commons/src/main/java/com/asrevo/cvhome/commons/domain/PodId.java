package com.asrevo.cvhome.commons.domain;

import org.bson.types.ObjectId;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.util.Objects;

public record PodId(@JsonSerialize(using = ToStringSerializer.class) ObjectId id) implements Identifier {

	public PodId(String id) {
		this(new ObjectId(id));
	}

	public static PodId newId() {
		return new PodId(new ObjectId());
	}

	@JsonSerialize(using = ToStringSerializer.class)
	@Override
	public ObjectId getId() {
		return this.id;
	}

	public String shorten() {
		if (Objects.isNull(id) || id.toString().isBlank()) {
			return null;
		}
		String idString = id.toString();
		return idString.substring(0, 8);
	}
}
