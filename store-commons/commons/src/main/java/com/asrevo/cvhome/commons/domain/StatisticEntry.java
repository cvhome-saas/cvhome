package com.asrevo.cvhome.commons.domain;

public record StatisticEntry(String date, String name, Number value) {
	public static StatisticEntry of(String date, String name, Number value) {
		return new StatisticEntry(date, name, value);
	}

	public static StatisticEntry of(String name, Number value) {
		return new StatisticEntry(null, name, value);
	}
}
