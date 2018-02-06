package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.cn.TenderDto;
import org.springframework.stereotype.Service;

@Service
public interface CnService {

    ResponseDto createCn(String owner, TenderDto tenderDto);

    ResponseDto updateCn(String owner,
                         String cpId,
                         String token,
                         TenderDto tenderDto);
}
