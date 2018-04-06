package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.tender.TenderProcessDto;
import java.time.LocalDateTime;

public interface TenderProcessService {

    ResponseDto createCn(String stage,
                         String country,
                         String owner,
                         LocalDateTime dateTime,
                         TenderProcessDto dto);


    ResponseDto updateCn(String cpId,
                         String token,
                         String owner,
                         TenderProcessDto dto);
}
