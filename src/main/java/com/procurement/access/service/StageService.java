package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface StageService {

    ResponseDto startNewStage(final String cpId,
                              final String token,
                              final String previousStage,
                              final String stage,
                              final String owner);
}
