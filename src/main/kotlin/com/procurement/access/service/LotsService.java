package com.procurement.access.service;

import com.procurement.access.model.dto.lots.LotsRequestDto;
import org.springframework.stereotype.Service;

@Service
public interface LotsService {

    ResponseDto getLots(String cpId, String stage, TenderStatus status);

    ResponseDto updateStatus(String cpId, String stage, TenderStatus status, LotsRequestDto lotsDto);

    ResponseDto updateStatusDetails(String cpId, String stage, TenderStatusDetails statusDetails, LotsRequestDto lotsDto);

    ResponseDto updateStatusDetailsById(String cpId, String stage, String lotId, TenderStatusDetails statusDetails);

    ResponseDto checkStatusDetails(String cpId, String stage);

    ResponseDto updateLots(String cpId, String stage, LotsRequestDto lotsDto);

}
