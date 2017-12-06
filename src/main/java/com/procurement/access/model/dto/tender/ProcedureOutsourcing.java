package com.procurement.access.model.dto.tender;

import lombok.Data;

@Data
public class ProcedureOutsourcing {
    public Boolean procedureOutsourced;
    public Organization outsourcedTo;
}
