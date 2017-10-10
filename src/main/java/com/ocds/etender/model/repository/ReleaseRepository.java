package com.ocds.etender.model.repository;

import com.ocds.etender.model.entity.Release;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ReleaseRepository extends CrudRepository<Release, String> {

	@Query("select * from etender.messages where auctionId=?0")
	List<Release> findByAuctionId(String auctionId);

}
