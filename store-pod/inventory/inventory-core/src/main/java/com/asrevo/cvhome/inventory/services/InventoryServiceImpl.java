package com.asrevo.cvhome.inventory.services;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.entity.Inventory;
import com.asrevo.cvhome.inventory.entity.InventoryPrice;
import com.asrevo.cvhome.inventory.model.PersistableInventory;
import com.asrevo.cvhome.inventory.model.PersistablePrice;
import com.asrevo.cvhome.inventory.model.PersistableSkuInventory;
import com.asrevo.cvhome.inventory.model.SkuInventory;
import com.asrevo.cvhome.inventory.repositories.InventoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SkuInventory> getBySkus(StoreMerchantId store, Collection<String> skus) {
        if (skus.isEmpty()) {
            return List.of();
        }
        LocalDate today = LocalDate.now();
        // Legacy data may hold several rows per sku; the first by id wins, matching the reservation path.
        Map<String, SkuInventory> bySku = new LinkedHashMap<>();
        for (Inventory inventory : inventoryRepository.findBySkus(store, skus)) {
            bySku.putIfAbsent(inventory.getSku(), SkuInventoryMapper.toSkuInventory(inventory, today));
        }
        return List.copyOf(bySku.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkuInventory> getByProductIds(StoreMerchantId store, Collection<Long> productIds) {
        if (productIds.isEmpty()) {
            return List.of();
        }
        LocalDate today = LocalDate.now();
        // Same first-row-per-sku rule as getBySkus: legacy data may hold duplicates and every reader must agree.
        Map<String, SkuInventory> bySku = new LinkedHashMap<>();
        for (Inventory inventory : inventoryRepository.findByProductIds(store, productIds)) {
            bySku.putIfAbsent(inventory.getSku(), SkuInventoryMapper.toSkuInventory(inventory, today));
        }
        return List.copyOf(bySku.values());
    }

    @Override
    @Transactional
    public SkuInventory upsert(StoreMerchantId store, String sku, PersistableInventory source) {
        Inventory inventory = inventoryRepository.findBySku(store, sku).orElseGet(() -> new Inventory(store, sku));
        inventory.setProductId(source.productId());
        inventory.setQuantity(source.quantity());
        inventory.setAvailable(source.available());
        if (source.quantityOrderMinimum() != null) {
            inventory.setQuantityOrderMinimum(source.quantityOrderMinimum());
        }
        if (source.quantityOrderMaximum() != null) {
            inventory.setQuantityOrderMaximum(source.quantityOrderMaximum());
        }
        applyPrice(inventory, source.price());
        return SkuInventoryMapper.toSkuInventory(inventoryRepository.save(inventory), LocalDate.now());
    }

    private void applyPrice(Inventory inventory, PersistablePrice source) {
        InventoryPrice price = inventory.defaultPrice().orElseGet(() -> {
            InventoryPrice created = new InventoryPrice(inventory);
            inventory.getPrices().add(created);
            return created;
        });
        price.setDefaultPrice(true);
        price.setAmount(source.amount());
        price.setSpecialAmount(source.specialAmount());
        price.setSpecialStartDate(source.specialStartDate());
        price.setSpecialEndDate(source.specialEndDate());
    }

    @Override
    @Transactional
    public List<SkuInventory> bulkUpsert(StoreMerchantId store, List<PersistableSkuInventory> entries) {
        return entries.stream().map(entry -> upsert(store, entry.sku(), entry.inventory())).toList();
    }

    @Override
    @Transactional
    public void deleteByProduct(StoreMerchantId store, Long productId) {
        inventoryRepository.deleteAll(inventoryRepository.findByStoreMerchantIdAndProductId(store, productId));
    }

    @Override
    @Transactional
    public void deleteBySku(StoreMerchantId store, String sku) {
        inventoryRepository.findBySku(store, sku).ifPresent(inventoryRepository::delete);
    }
}
