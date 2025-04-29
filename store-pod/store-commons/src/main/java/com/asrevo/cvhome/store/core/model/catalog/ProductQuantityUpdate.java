package com.asrevo.cvhome.store.core.model.catalog;

import com.asrevo.cvhome.commons.domain.Entry;
import java.util.List;

public record ProductQuantityUpdate(List<Entry<String, Integer>> newAvailabilitiesBySku) {}
