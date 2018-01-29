package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.lots.LotsRequestDto;
import org.springframework.stereotype.Service;

@Service
public interface TenderService {

    ResponseDto updateStatus(String cpId, String status);

    ResponseDto updateStatusDetails(String cpId, String statusDetails);
}
