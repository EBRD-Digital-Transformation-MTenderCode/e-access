package com.ocds.etender.model.entity;

import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

@Table("releases")
public class Release {
    @PrimaryKey
    private String ocid;

    private String auctionId;

    private Integer currentRound;
}
