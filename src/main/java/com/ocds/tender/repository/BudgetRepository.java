package com.ocds.tender.repository;

import com.ocds.tender.model.entity.BudgetEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetRepository extends CassandraRepository<BudgetEntity> {

}
