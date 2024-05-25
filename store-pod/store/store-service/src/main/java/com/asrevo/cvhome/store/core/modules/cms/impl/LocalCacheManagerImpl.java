package com.asrevo.cvhome.store.core.modules.cms.impl;

/**
 * Http server bootstrap
 *
 * @param rootName file location root
 * @author carlsamson
 */
public record LocalCacheManagerImpl(String rootName) implements CMSManager {

    @Override
    public String getLocation() {
        return "";
    }


}
