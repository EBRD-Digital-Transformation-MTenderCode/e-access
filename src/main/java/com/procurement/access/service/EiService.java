package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ei.EiDto;
import org.springframework.stereotype.Service;

@Service
public interface EiService {

    ResponseDto createEi(String owner,
                         EiDto eiDto);

    ResponseDto updateEi(String owner,
                         String cpId,
                         String token,
                         EiDto eiDto);
}
