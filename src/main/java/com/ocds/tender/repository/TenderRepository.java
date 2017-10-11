package com.ocds.tender.repository;

import com.ocds.tender.model.entity.TenderEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenderRepository extends CassandraRepository<TenderEntity> {

}
