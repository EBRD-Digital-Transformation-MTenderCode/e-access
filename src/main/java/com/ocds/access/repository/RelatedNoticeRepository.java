package com.ocds.access.repository;

import com.ocds.access.model.entity.RelatedNoticeEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RelatedNoticeRepository extends CassandraRepository<RelatedNoticeEntity, String> {

    @Query(value = "select * from budget where oc_id=?0 LIMIT 1")
    RelatedNoticeEntity getLastByOcId(String ocId);
}
