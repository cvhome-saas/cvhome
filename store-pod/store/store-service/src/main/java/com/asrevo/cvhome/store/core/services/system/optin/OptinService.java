package com.asrevo.cvhome.store.core.services.system.optin;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.system.optin.Optin;
import com.asrevo.cvhome.store.core.entity.system.optin.OptinType;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

/**
 * Registers Optin events
 *
 * @author carlsamson
 */
public interface OptinService extends SalesManagerEntityService<Long, Optin> {

    Optin getOptinByMerchantAndType(MerchantStore store, OptinType type) throws ServiceException;

    Optin getOptinByCode(MerchantStore store, String code) throws ServiceException;
}
