package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.cn.CnProcess;
import java.time.LocalDateTime;

public interface CnOnPnService {

    ResponseDto createCnOnPn(
        String cpId,
        String token,
        String owner,
        String stage,
        String previousStage,
        LocalDateTime dateTime,
        CnProcess data);
}
