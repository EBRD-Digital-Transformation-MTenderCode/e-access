package com.procurement.access.service;

import com.procurement.access.model.dto.pn.PnProcess;
import java.time.LocalDateTime;

public interface PNService {

    ResponseDto createPn(String stage,
                         String country,
                         String owner,
                         LocalDateTime dateTime,
                         PnProcess dto);
}
