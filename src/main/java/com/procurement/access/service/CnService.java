package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.tender.TenderDto;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public interface CnService {

    ResponseDto createCn(String owner,
                         LocalDateTime dateTime,
                         TenderDto tenderDto);

    ResponseDto updateCn(String owner,
                         String cpId,
                         String token,
                         TenderDto tenderDto);
}
