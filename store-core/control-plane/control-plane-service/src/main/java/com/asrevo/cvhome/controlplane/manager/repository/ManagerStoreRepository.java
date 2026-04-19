package com.asrevo.cvhome.controlplane.manager.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.controlplane.manager.entity.ManagerStoreEntity;

public interface ManagerStoreRepository
        extends CrudRepository<ManagerStoreEntity, ManagerStoreId>, QueryByExampleExecutor<ManagerStoreEntity> {

    Boolean existsByName(String name);

    @Query("""
             select date(m.created_date) as date,count(date(m.created_date)) as value
             from manager.manager_store m
             where m.created_date between  :from and :to
            group by date(m.created_date)""")
    List<StatisticEntry> storeStatistic(Instant from, Instant to);

}
