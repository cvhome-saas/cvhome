package com.asrevo.cvhome.store.utils;

import java.util.Objects;

public final class NumberUtils {

    private NumberUtils() {

    }

    public static boolean isPositive(Long id) {
        return Objects.nonNull(id) && id > 0;
    }

}
