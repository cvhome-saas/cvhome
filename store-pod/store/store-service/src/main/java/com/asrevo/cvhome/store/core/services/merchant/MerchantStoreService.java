package com.asrevo.cvhome.store.core.services.merchant;

import com.asrevo.cvhome.store.core.entity.common.GenericEntityList;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStoreCriteria;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface MerchantStoreService extends SalesManagerEntityService<Integer, MerchantStore> {


    MerchantStore getByCode(String code) throws ServiceException;

    MerchantStore getDefaultStore();

    MerchantStore getParent(String code) throws ServiceException;

    List<MerchantStore> findAllStoreNames() throws ServiceException;

    List<MerchantStore> findAllStoreNames(String code) throws ServiceException;

    List<MerchantStore> findAllStoreCodeNameEmail() throws ServiceException;

    Page<MerchantStore> listAll(Optional<String> storeName, int page, int count) throws ServiceException;

    Page<MerchantStore> listByGroup(Optional<String> storeName, String code, int page, int count) throws ServiceException;

    Page<MerchantStore> listAllRetailers(Optional<String> storeName, int page, int count) throws ServiceException;

    Page<MerchantStore> listChildren(String code, int page, int count) throws ServiceException;

    boolean existByCode(String code);

    /**
     * Is parent or child and part of a specific group
     *
     * @param code
     * @return
     */
    boolean isStoreInGroup(String code) throws ServiceException;

    void saveOrUpdate(MerchantStore store) throws ServiceException;

    GenericEntityList<MerchantStore> getByCriteria(MerchantStoreCriteria criteria) throws ServiceException;

}
