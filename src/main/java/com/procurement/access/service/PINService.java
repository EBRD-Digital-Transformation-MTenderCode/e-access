package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.pin.PinDto;
import java.time.LocalDateTime;

public interface PINService {

    ResponseDto createPin(String stage,
                          String country,
                          String owner,
                          LocalDateTime dateTime,
                          PinDto dto);
}
