package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface StageService {

    ResponseDto startNewStage(String cpId,
                              String token,
                              String previousStage,
                              String newStage,
                              String owner);
}
