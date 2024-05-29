package com.asrevo.cvhome.store.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.stereotype.Component;

@Component("cache")
@Slf4j
public class CacheUtils {

    public final static String REFERENCE_CACHE = "REF";
    private final static String KEY_DELIMITER = "_";
    //	@TODO ASHRAF
//    @Autowired
//    @Qualifier("serviceCache")
    private final Cache cache = new ConcurrentMapCache("ashraf-checks");

    public void putInCache(Object object, String keyName) throws Exception {

        cache.put(keyName, object);

    }


    public Object getFromCache(String keyName) throws Exception {

        ValueWrapper vw = cache.get(keyName);
        if (vw != null) {
            return vw.get();
        }

        return null;

    }


}
