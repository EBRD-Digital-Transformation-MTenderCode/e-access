package com.ocds.tender.repository;

import com.ocds.tender.model.entity.EventLogEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventLogRepository extends CassandraRepository<EventLogEntity> {

}
