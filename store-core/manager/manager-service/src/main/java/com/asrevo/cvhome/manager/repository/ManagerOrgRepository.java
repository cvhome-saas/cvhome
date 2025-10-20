package com.asrevo.cvhome.manager.repository;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.manager.entity.ManagerOrgEntity;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

public interface ManagerOrgRepository
        extends CrudRepository<ManagerOrgEntity, ManagerOrgId>,
                QueryByExampleExecutor<ManagerOrgEntity>,
                PagingAndSortingRepository<ManagerOrgEntity, ManagerOrgId> {
    @Query(
            """
             select m.subscription_plan as name,count(m.subscription_plan) as value
             from manager.manager_org m
             where m.created_date between  :from and :to
            group by m.subscription_plan""")
    List<StatisticEntry> subscriptionStatistic(Instant from, Instant to);

    @Query(
            """
             select date(m.created_date) as date,count(date(m.created_date)) as value
             from manager.manager_org m
             where m.created_date between  :from and :to
            group by date(m.created_date)""")
    List<StatisticEntry> orgStatistic(Instant from, Instant to);
}
