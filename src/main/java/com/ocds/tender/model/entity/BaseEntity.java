package com.ocds.tender.model.entity;

import com.datastax.driver.core.utils.UUIDs;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;

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

    @Column(value = "id")
    private UUID id;

    public BaseEntity() {
        this.id = UUIDs.random();
    }
}
