package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ocds.TenderStatus;
import com.procurement.access.model.dto.ocds.TenderStatusDetails;
import org.springframework.stereotype.Service;

@Service
public interface TenderService {

    ResponseDto updateStatus(String cpId, TenderStatus status);

    ResponseDto updateStatusDetails(String cpId, TenderStatusDetails statusDetails);
}
