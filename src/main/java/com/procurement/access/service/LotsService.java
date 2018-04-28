package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.lots.LotsRequestDto;
import com.procurement.access.model.dto.ocds.TenderStatus;
import com.procurement.access.model.dto.ocds.TenderStatusDetails;
import org.springframework.stereotype.Service;

@Service
public interface LotsService {

    ResponseDto getLots(String cpId, String stage, TenderStatus status);

    ResponseDto updateStatus(String cpId, String stage, TenderStatus status, LotsRequestDto lotsDto);

    ResponseDto updateStatusDetails(String cpId, String stage, TenderStatusDetails statusDetails, LotsRequestDto lotsDto);

    ResponseDto updateStatusDetailsById(String cpId, String stage, String lotId, TenderStatusDetails statusDetails);
}
