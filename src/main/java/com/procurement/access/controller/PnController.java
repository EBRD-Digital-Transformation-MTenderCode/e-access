package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.pn.PnProcess;
import com.procurement.access.service.PNService;
import java.time.LocalDateTime;
import javax.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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
                                                @RequestParam(value = "pmd", required = false) final String pmd,
                                                @RequestParam("owner") final String owner,
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                @RequestParam("date") final LocalDateTime dateTime,
                                                @Valid @RequestBody final PnProcess data) {
        return new ResponseEntity<>(
                pnService.createPn(stage, country, owner, dateTime, data),
                HttpStatus.CREATED);
    }
}
