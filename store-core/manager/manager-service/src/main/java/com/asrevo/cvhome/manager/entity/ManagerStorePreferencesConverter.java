package com.asrevo.cvhome.manager.entity;

import com.asrevo.cvhome.commons.domain.ManagerStorePreferences;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

public class ManagerStorePreferencesConverter {

	@WritingConverter
	public static class ManagerStorePreferencesWrittingConverter implements Converter<ManagerStorePreferences, String> {

		private final ObjectMapper objectMapper = new ObjectMapper();

		@Override
		public String convert(ManagerStorePreferences source) {
			try {
				return objectMapper.writeValueAsString(source);
			}
			catch (JsonProcessingException e) {
				throw new RuntimeException("Failed to convert Preferences to JSON", e);
			}
		}

	}

	@ReadingConverter
	public static class ManagerStorePreferencesReadingConverter implements Converter<String, ManagerStorePreferences> {

		private final ObjectMapper objectMapper = new ObjectMapper();

		@Override
		public ManagerStorePreferences convert(String source) {
			try {
				return objectMapper.readValue(source, ManagerStorePreferences.class);
			}
			catch (JsonProcessingException e) {
				throw new RuntimeException("Failed to convert Preferences to JSON", e);
			}
		}

	}

}
