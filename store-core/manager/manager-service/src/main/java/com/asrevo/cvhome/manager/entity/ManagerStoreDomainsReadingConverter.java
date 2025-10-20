package com.asrevo.cvhome.manager.entity;

import java.util.Collection;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class ManagerStoreDomainsReadingConverter
		implements Converter<Collection<ManagerStoreDomain>, ManagerStoreDomains> {

	@Override
	public ManagerStoreDomains convert(Collection<ManagerStoreDomain> source) {
		return ManagerStoreDomains.of(source);
	}

}
