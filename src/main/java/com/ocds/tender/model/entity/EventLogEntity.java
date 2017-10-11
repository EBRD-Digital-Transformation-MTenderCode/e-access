package com.ocds.tender.model.entity;

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
@Table("event_log")
public class EventLogEntity {

    @PrimaryKeyColumn(name = "oc_id", type = PrimaryKeyType.PARTITIONED)
    private String ocId;

    @PrimaryKeyColumn(name = "added_date", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private Date addedDate;

    @Column(value = "event_type")
    private String eventType;

    @Column(value = "id")
    private UUID id;
}