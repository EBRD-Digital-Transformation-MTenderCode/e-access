package com.procurement.access.repository;

import com.procurement.access.model.entity.EinEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EinRepository extends CassandraRepository<EinEntity, String> {

    @Query(value = "select * from access_ein where cp_id=?0 LIMIT 1")
    EinEntity getLastByCpId(String cpId);
}
