package com.ocds.etender.model.dto.tender;

import lombok.Data;

@Data
public class Details {
    public String typeOfBuyer;
    public String mainGeneralActivity;
    public String mainSectoralActivity;
    public Boolean isACentralPurchasingBody;
    public String NUTSCode;
}
