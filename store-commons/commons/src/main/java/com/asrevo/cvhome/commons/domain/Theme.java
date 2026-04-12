package com.asrevo.cvhome.commons.domain;

import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Theme {

	BASIS(true), MODERN(true), JEWELERY(true), BEAUTY(true), FURNITURE(false), SPORTS(false), ELECTRONICS(false),
	FOOD(false), GLASSES(false), COSMETICS(false), WATCHES(false), BABY(false), TOOLS(false);

	private final boolean implemented;

	public static List<Theme> getImplementedThemes() {
		return Arrays.stream(Theme.values()).filter(theme -> theme.implemented).toList();
	}

}
