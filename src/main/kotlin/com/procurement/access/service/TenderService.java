package com.procurement.access.service;

import org.springframework.stereotype.Service;

@Service
public interface TenderService {

    ResponseDto updateStatus(String cpId, String stage, TenderStatus status);

    ResponseDto updateStatusDetails(String cpId, String stage, TenderStatusDetails statusDetails);

    ResponseDto setSuspended(String cpId, String stage, Boolean suspended);

    ResponseDto setUnsuccessful(String cpId, String stage);
}
