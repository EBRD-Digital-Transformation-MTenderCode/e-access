package com.procurement.access.service;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.pn.PlanningNoticeDto;
import com.procurement.access.model.dto.tender.TenderProcessDto;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public interface PlanningNoticeService {

    ResponseDto createPn(String stage,
                         String country,
                         String owner,
                         PlanningNoticeDto dto);
}
