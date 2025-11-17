package com.asrevo.cvhome.subscription.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public record ToJsonObj(Gson gson) {
	public ToJsonObj() {
		this(new GsonBuilder().create());
	}

	public JsonObject exec(String json) {
		return gson.fromJson(json, JsonObject.class);
	}
}
