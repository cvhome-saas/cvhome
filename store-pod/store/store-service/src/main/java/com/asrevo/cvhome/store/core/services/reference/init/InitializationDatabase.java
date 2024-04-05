package com.asrevo.cvhome.store.core.services.reference.init;

import com.asrevo.cvhome.store.core.exception.ServiceException;

public interface InitializationDatabase {

    boolean isEmpty();

    void populate(String name) throws ServiceException;

}
