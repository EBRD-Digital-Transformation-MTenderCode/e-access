package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.cn.CnProcess;
import java.time.LocalDateTime;

public interface CnService {

    ResponseDto createCn(String stage,
                         String country,
                         String owner,
                         LocalDateTime dateTime,
                         CnProcess dto);


    ResponseDto updateCn(String cpId,
                         String token,
                         String owner,
                         CnProcess dto);
}
