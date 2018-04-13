package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.pin.PinProcess;
import java.time.LocalDateTime;

public interface PinOnPnService {

    ResponseDto createPinOnPn(
            String cpId,
            String token,
            String owner,
            String stage,
            String previousStage,
            LocalDateTime dateTime,
            PinProcess data);
}
