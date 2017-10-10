package com.ocds.etender.repository;

import com.ocds.etender.model.entity.BudgetEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetRepository extends CrudRepository<BudgetEntity, String> {

}
