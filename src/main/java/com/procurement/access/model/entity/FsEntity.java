package com.procurement.access.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Getter
@Setter
@Table("access_fs")
public class FsEntity {

    @PrimaryKeyColumn(name = "cp_id", type = PrimaryKeyType.PARTITIONED)
    private String cpId;

    @PrimaryKeyColumn(name = "oc_id", type = PrimaryKeyType.CLUSTERED)
    private String ocId;

    @Column(value = "amount")
    private Double amount;

    @Column(value = "json_data")
    private String jsonData;
}
