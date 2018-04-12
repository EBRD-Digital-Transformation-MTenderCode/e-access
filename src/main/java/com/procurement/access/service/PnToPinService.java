package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.pnToPin.PnToPinDto;
import java.time.LocalDateTime;

public interface PnToPinService {

    ResponseDto createPinfromPn(
        String id,
        String previosStage,
        String stage,
        String owner,
        String token,
        LocalDateTime dateTime,
        PnToPinDto data);
}
