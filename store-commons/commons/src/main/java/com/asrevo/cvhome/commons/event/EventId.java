package com.asrevo.cvhome.commons.event;

import com.asrevo.cvhome.commons.domain.Identifier;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;

public record EventId(@JsonSerialize(using = ToStringSerializer.class) ObjectId id) implements Identifier {
	public EventId(String id) {
		this(new ObjectId(id));
	}

	public static EventId newId() {
		return new EventId(new ObjectId());
	}

	@JsonSerialize(using = ToStringSerializer.class)
	@Override
	public ObjectId getId() {
		return this.id;
	}
}
