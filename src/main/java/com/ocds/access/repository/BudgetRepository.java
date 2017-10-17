package com.ocds.access.repository;

import com.ocds.access.model.entity.BudgetEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetRepository extends CassandraRepository<BudgetEntity, String> {

    @Query(value = "select * from budget where oc_id=?0 LIMIT 1")
    BudgetEntity getLastByOcId(String ocId);
}
