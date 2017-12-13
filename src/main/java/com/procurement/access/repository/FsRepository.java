package com.procurement.access.repository;

import com.procurement.access.model.entity.FsEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FsRepository extends CassandraRepository<FsEntity, String> {

    @Query(value = "select sum(amount) from access_fs where cpid=?0")
    Double getTotalAmountByCpId(String cpId);
}
