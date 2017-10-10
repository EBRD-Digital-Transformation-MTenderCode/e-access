package com.ocds.etender.repository;

import com.ocds.etender.model.entity.RelatedNoticeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelatedNoticeRepository extends CrudRepository<RelatedNoticeEntity, String> {

}
