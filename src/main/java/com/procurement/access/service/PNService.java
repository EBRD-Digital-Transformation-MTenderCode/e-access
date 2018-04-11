package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.pn.PnDto;
import java.time.LocalDateTime;

public interface PNService {

    ResponseDto createPn(String stage,
                         String country,
                         String owner,
                         LocalDateTime dateTime,
                         PnDto dto);
}
