package com.ocds.etender.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Table("event")
public class EventEntity {

    @PrimaryKeyColumn(name = "oc_id", type = PrimaryKeyType.PARTITIONED)
    private String ocId;

    @PrimaryKeyColumn(name = "added_date", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private Date addedDate;

    @Column(value = "type")
    private String type;

    @Column(value = "id")
    private UUID id;
}