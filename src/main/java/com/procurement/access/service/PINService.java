package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.pin.PinDto;
import com.procurement.access.model.dto.pn.PnDto;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

public interface PINService {

    ResponseDto createPin(String stage,
                         String country,
                         String owner,
                         LocalDateTime dateTime,
                          PinDto dto);
}
