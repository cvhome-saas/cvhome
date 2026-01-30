package com.asrevo.cvhome.commons.event;

import com.asrevo.cvhome.commons.domain.Identifier;
import org.bson.types.ObjectId;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

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
