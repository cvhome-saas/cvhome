package com.asrevo.cvhome.store.utils;

import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import java.util.List;
import org.springframework.data.domain.Page;

public final class ReadableEntityUtil {

	private ReadableEntityUtil() {
	}

	public static <T, R> ReadableEntityList<R> createReadableList(Page<T> entityList, List<R> items) {
		ReadableEntityList<R> readableList = new ReadableEntityList<>();
		readableList.setContent(items);
		readableList.setTotalPages(entityList.getTotalPages());
		readableList.setTotalElements(entityList.getTotalElements());
		readableList.setSize(items.size());
		readableList.setPageNumber(entityList.getNumber());
		return readableList;
	}

}
