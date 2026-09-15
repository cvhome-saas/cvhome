package com.asrevo.cvhome.commons.domain;

/**
 * A value that may take part in a cache key.
 *
 * <p>
 * A cache key is built from typed parts and rendered to one string, so every part must say how it reads in that
 * string, and only a type that implements this can be a part: a raw {@code Long} or {@code String} id is refused,
 * because the key would then say nothing about what it identifies. Value objects implement it; the storefront's
 * criteria objects are hashed into a {@code QueryHash} first.
 * </p>
 */
public interface KeyPart {

    /** The part as it reads in a rendered key: short, stable, without the separator {@code |}. */
    String cacheKeyPart();

}
