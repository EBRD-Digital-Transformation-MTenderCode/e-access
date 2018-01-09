package com.procurement.access.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Getter
@Setter
@Table("access_cn")
public class CnEntity {

    @PrimaryKeyColumn(name = "cp_id", type = PrimaryKeyType.PARTITIONED)
    private String cpId;

    @PrimaryKeyColumn(name = "token_entity", type = PrimaryKeyType.CLUSTERED)
    private String tokenEntity;

    @Column(value = "owner")
    private String owner;

    @Column(value = "json_data")
    private String jsonData;
}
