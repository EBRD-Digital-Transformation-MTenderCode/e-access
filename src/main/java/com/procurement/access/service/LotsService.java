package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.lots.LotsRequestDto;
import com.procurement.access.model.dto.ocds.TenderStatus;
import com.procurement.access.model.dto.ocds.TenderStatusDetails;
import org.springframework.stereotype.Service;

@Service
public interface LotsService {

    ResponseDto getLots(String cpId, TenderStatus status);

    ResponseDto updateStatus(String cpId, TenderStatus status, LotsRequestDto lotsDto);

    ResponseDto updateStatusDetails(String cpId, TenderStatusDetails statusDetails, LotsRequestDto lotsDto);
}
