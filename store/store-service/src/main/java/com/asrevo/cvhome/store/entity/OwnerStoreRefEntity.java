package com.asrevo.cvhome.store.entity;

import com.asrevo.cvhome.store.commons.domain.StoreId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("owner_store_ref")
@Getter
@Setter
public class OwnerStoreRefEntity {
    @Column("store_id")
    private AggregateReference<StoreEntity, StoreId> store;
}
