package com.ocds.tender.model.entity;

import com.datastax.driver.core.utils.UUIDs;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class BaseEntity {

    @PrimaryKeyColumn(name = "oc_id", type = PrimaryKeyType.PARTITIONED)
    private String ocId;

    @PrimaryKeyColumn(name = "added_date", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private Date addedDate;

    @Column(value = "json_data")
    private String jsonData;

    @Column(value = "event_type")
    private String eventType;

    @Column(value = "id")
    private UUID id;

    public BaseEntity() {
        this.id = UUIDs.random();
    }
}
