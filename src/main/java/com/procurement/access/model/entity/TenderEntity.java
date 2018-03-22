package com.procurement.access.model.entity;

import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TenderEntity {

    private String cpId;

    private UUID token;

    private String owner;

    private Date createdDate;

    private String jsonData;
}
