package com.procurement.access.service;

import com.procurement.access.model.dto.cn.CnProcess;
import java.time.LocalDateTime;

public interface CnOnPinService {

    ResponseDto createCnOnPin(
            String cpId,
            String previousStage,
            String stage,
            String owner,
            String token,
            LocalDateTime dateTime,
            CnProcess data);

}
