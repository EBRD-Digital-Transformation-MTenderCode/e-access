package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.pn.PlanningNoticeDto;
import com.procurement.access.service.PNService;
import java.time.LocalDateTime;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/pn")
public class PnController {

    private final PNService pnService;

    public PnController(final PNService pnService) {
        this.pnService = pnService;
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createPn(@RequestParam("stage") final String stage,
                                                @RequestParam("country") final String country,
                                                @RequestParam("pmd") final String pmd,
                                                @RequestParam("owner") final String owner,
                                                @RequestParam("date") final LocalDateTime dateTime,
                                                @Valid @RequestBody final PlanningNoticeDto data) {
        return new ResponseEntity<>(
                pnService.createPn(stage, country, owner, dateTime, data),
                HttpStatus.CREATED);
    }
}
