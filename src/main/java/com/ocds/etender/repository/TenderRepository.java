package com.ocds.etender.repository;

import com.ocds.etender.model.entity.TenderEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenderRepository extends CrudRepository<TenderEntity, String> {

}
