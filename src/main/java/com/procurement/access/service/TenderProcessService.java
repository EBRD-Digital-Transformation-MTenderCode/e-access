package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.tender.TenderProcessRequestDto;
import com.procurement.access.model.dto.tender.TenderProcessResponseDto;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public interface TenderProcessService {

    ResponseDto createCn(String stage,
                         String country,
                         String owner,
                         LocalDateTime dateTime,
                         TenderProcessRequestDto dto);


    ResponseDto updateCn(String cpId,
                         String token,
                         String owner,
                         TenderProcessResponseDto dto);

    ResponseDto createPin(String stage,
                         String country,
                         String owner,
                         LocalDateTime dateTime,
                         TenderProcessResponseDto dto);

}
