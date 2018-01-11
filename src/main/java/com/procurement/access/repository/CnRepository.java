package com.procurement.access.repository;

import com.procurement.access.model.entity.CnEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CnRepository extends CassandraRepository<CnEntity, String> {

    @Query(value = "select * from access_cn where oc_id=?0 LIMIT 1")
    CnEntity getLastByOcId(String ocId);
}
