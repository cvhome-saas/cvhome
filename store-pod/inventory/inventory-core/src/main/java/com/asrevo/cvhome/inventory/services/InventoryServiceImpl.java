package com.asrevo.cvhome.inventory.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.Sku;
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
    public List<SkuInventory> getBySkus(StoreMerchantId store, Collection<Sku> skus) {
        if (skus.isEmpty()) {
            return List.of();
        }
        LocalDate today = LocalDate.now();
        // Legacy data may hold several rows per sku; the first by id wins, matching the reservation path.
        Map<Sku, SkuInventory> bySku = new LinkedHashMap<>();
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
        Map<Sku, SkuInventory> bySku = new LinkedHashMap<>();
        for (Inventory inventory : inventoryRepository.findByProductIds(store, productIds)) {
            bySku.putIfAbsent(inventory.getSku(), SkuInventoryMapper.toSkuInventory(inventory, today));
        }
        return List.copyOf(bySku.values());
    }

    @Override
    @Transactional
    public SkuInventory upsert(StoreMerchantId store, Sku sku, PersistableInventory source) {
        Inventory inventory = inventoryRepository.findBySku(store, sku).orElseGet(() -> new Inventory(store, sku));
        return write(inventory, source, LocalDate.now());
    }

    private SkuInventory write(Inventory inventory, PersistableInventory source, LocalDate today) {
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
        inventory.stockChanged().priceChanged();
        return SkuInventoryMapper.toSkuInventory(inventoryRepository.save(inventory), today);
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
        // The entries were validated against Sku.FORMAT at the edge, so each converts.
        List<Sku> skus = entries.stream().map(entry -> Sku.of(entry.sku())).toList();
        // One read for the whole batch, not one per sku (21 statements for 20 skus in the 2026-09-14 load test). The
        // first row by id wins, as in getBySkus; a sku listed twice edits the one row, as two single upserts would.
        Map<Sku, Inventory> rows = new HashMap<>();
        for (Inventory inventory : inventoryRepository.findBySkus(store, skus)) {
            rows.putIfAbsent(inventory.getSku(), inventory);
        }
        LocalDate today = LocalDate.now();
        List<SkuInventory> written = new ArrayList<>(entries.size());
        for (int i = 0; i < entries.size(); i++) {
            Inventory inventory = rows.computeIfAbsent(skus.get(i), sku -> new Inventory(store, sku));
            written.add(write(inventory, entries.get(i).inventory(), today));
        }
        return written;
    }

    @Override
    @Transactional
    public void deleteByProduct(StoreMerchantId store, Long productId) {
        inventoryRepository.deleteAll(inventoryRepository.findByStoreMerchantIdAndProductId(store, productId));
    }

    @Override
    @Transactional
    public void deleteBySku(StoreMerchantId store, Sku sku) {
        inventoryRepository.findBySku(store, sku).ifPresent(inventoryRepository::delete);
    }
}
