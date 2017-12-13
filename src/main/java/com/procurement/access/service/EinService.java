package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ein.UpdateFsDto;
import com.procurement.access.model.dto.ein.EinDto;
import org.springframework.stereotype.Service;

@Service
public interface EinService {

    ResponseDto createEin(EinDto einDto);

    ResponseDto updateEin(EinDto einDto);

    ResponseDto updateFs(UpdateFsDto updateFsDto);
}
