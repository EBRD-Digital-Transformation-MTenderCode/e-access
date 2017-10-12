package com.ocds.tender.repository;

import com.ocds.tender.model.entity.TenderEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TenderRepository extends CassandraRepository<TenderEntity, String> {

    @Query(value = "select * from tender where oc_id=?0 LIMIT 1")
    TenderEntity getLastByOcId(String ocId);
}
