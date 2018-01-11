package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ein.EinDto;
import com.procurement.access.model.dto.ein.UpdateFsDto;
import org.springframework.stereotype.Service;

@Service
public interface EinService {

    ResponseDto createEin(String country,
                          String pmd,
                          String stage,
                          String owner,
                          EinDto einDto);

    ResponseDto updateEin(EinDto einDto);

    ResponseDto updateAmountByFs(String cpid);
}
