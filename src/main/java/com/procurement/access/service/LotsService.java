package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.lots.LotsRequestDto;
import com.procurement.access.model.dto.ocds.TenderStatus;
import org.springframework.stereotype.Service;

@Service
public interface LotsService {

    ResponseDto getLots(String cpId, String status);

    ResponseDto updateLotsStatus(String cpId, String status, LotsRequestDto lotsDto);

    ResponseDto updateLotsStatusDetails(String cpId, String statusDetails, LotsRequestDto lotsDto);
}
