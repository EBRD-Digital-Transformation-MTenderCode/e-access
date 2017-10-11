package com.ocds.tender.repository;

import com.ocds.tender.model.entity.RelatedNoticeEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelatedNoticeRepository extends CassandraRepository<RelatedNoticeEntity> {

}
