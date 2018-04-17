package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.cn.CnProcess;
import com.procurement.access.model.dto.pin.PinProcess;
import com.procurement.access.service.CnOnPnService;
import com.procurement.access.service.PinOnPnService;
import java.time.LocalDateTime;
import javax.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/cnOnPn")
public class CnOnPnController {

    private final CnOnPnService cnOnPnService;

    public CnOnPnController(final CnOnPnService cnOnPnService) {
        this.cnOnPnService = cnOnPnService;
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createCnOnPn(@RequestParam("identifier") final String cpId,
                                                     @RequestParam("previousStage") final String previousStage,
                                                     @RequestParam("stage") final String stage,
                                                     @RequestParam("country") final String country,
                                                     @RequestParam("pmd") final String pmd,
                                                     @RequestParam("owner") final String owner,
                                                     @RequestParam("token") final String token,
                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                     @RequestParam("date") final LocalDateTime dateTime,
                                                     @Valid @RequestBody final CnProcess data) {
        return new ResponseEntity<>(
            cnOnPnService.createCnOnPn(cpId, previousStage, stage, owner, token, dateTime, data),
                HttpStatus.CREATED);
    }
}
