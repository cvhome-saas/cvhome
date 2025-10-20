package com.asrevo.cvhome.commons.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;

public record ManagerStoreId(@JsonSerialize(using = ToStringSerializer.class) ObjectId id) implements Identifier {
	public ManagerStoreId(String id) {
		this(new ObjectId(id));
	}

	public static ManagerStoreId newId() {
		return new ManagerStoreId(new ObjectId());
	}

	@JsonSerialize(using = ToStringSerializer.class)
	@Override
	public ObjectId getId() {
		return this.id;
	}
}
