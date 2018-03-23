package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.tender.CnDto;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public interface CnService {

    ResponseDto createCn(String owner,
                         LocalDateTime dateTime,
                         CnDto dto);

    ResponseDto updateCn(String owner,
                         String cpId,
                         String token,
                         CnDto dto);
}
