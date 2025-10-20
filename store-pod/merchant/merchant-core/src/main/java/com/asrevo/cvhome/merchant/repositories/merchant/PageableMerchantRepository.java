package com.asrevo.cvhome.merchant.repositories.merchant;

import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PageableMerchantRepository extends PagingAndSortingRepository<MerchantStore, String> {

}
