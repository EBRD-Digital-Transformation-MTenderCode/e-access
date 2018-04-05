package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.pn.PlanningNoticeDto;
import com.procurement.access.service.PlanningNoticeService;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/pn")
public class PnController {

    private final PlanningNoticeService planningNotiseService;

    public PnController(final PlanningNoticeService planningNotiseService) {
        this.planningNotiseService = planningNotiseService;
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createPn(@RequestParam("stage") final String stage,
                                                @RequestParam("country") final String country,
                                                @RequestParam("pmd") final String pmd,
                                                @RequestParam("owner") final String owner,
                                                @Valid @RequestBody final PlanningNoticeDto data) {
        return new ResponseEntity<>(
            planningNotiseService.createPn(stage, country, owner, data),
                HttpStatus.CREATED);
    }
}
