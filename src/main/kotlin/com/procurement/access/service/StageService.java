package com.procurement.access.service;

import org.springframework.stereotype.Service;

@Service
public interface StageService {

    ResponseDto startNewStage(String cpId,
                              String token,
                              String previousStage,
                              String newStage,
                              String owner);
}
