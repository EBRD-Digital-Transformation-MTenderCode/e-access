package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ein.EinDto;
import org.springframework.stereotype.Service;

@Service
public interface EinService {

    ResponseDto createEin(String owner,
                          EinDto einDto);

    ResponseDto updateEin(String owner,
                          String cpId,
                          String token,
                          EinDto einDto);
}
