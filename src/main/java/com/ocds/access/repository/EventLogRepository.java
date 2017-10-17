package com.ocds.access.repository;

import com.ocds.access.model.entity.EventLogEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventLogRepository extends CassandraRepository<EventLogEntity, String> {

}
