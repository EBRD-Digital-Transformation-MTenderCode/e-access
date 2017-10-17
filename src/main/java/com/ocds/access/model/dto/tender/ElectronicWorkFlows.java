package com.ocds.access.model.dto.tender;

import lombok.Data;

@Data
public class ElectronicWorkFlows {
    public Boolean useOrdering;
    public Boolean usePayment;
    public Boolean acceptInvoicing;
}
