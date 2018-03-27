package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.tender.TenderProcessDto;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public interface TenderProcessService {

    ResponseDto createCn(String owner,
                         LocalDateTime dateTime,
                         TenderProcessDto dto);

    ResponseDto updateCn(String owner,
                         String cpId,
                         String token,
                         TenderProcessDto dto);
}
