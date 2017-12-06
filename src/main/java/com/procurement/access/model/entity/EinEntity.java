package com.procurement.access.model.entity;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Getter
@Setter
@Table("access_ein")
public class EinEntity {

    @PrimaryKeyColumn(name = "oc_id", type = PrimaryKeyType.PARTITIONED)
    private String ocId;

    @PrimaryKeyColumn(name = "ein_id", type = PrimaryKeyType.CLUSTERED)
    private UUID einId;

    @Column(value = "json_data")
    private String jsonData;
}
