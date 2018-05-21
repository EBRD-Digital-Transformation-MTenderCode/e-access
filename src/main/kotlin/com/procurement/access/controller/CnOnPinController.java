package com.procurement.access.controller;

import com.procurement.access.model.dto.cn.CnProcess;
import com.procurement.access.service.CnOnPinService;
import java.time.LocalDateTime;
import javax.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/cnOnPin")
public class CnOnPinController {

    private final CnOnPinService cnOnPinService;

    public CnOnPinController(final CnOnPinService cnOnPinService) {
        this.cnOnPinService = cnOnPinService;
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createPinOnPin(@RequestParam("identifier") final String cpId,
                                                      @RequestParam("previousStage") final String previousStage,
                                                      @RequestParam("stage") final String stage,
                                                      @RequestParam(value = "country", required = false) final String country,
                                                      @RequestParam(value = "pmd", required = false) final String pmd,
                                                      @RequestParam("owner") final String owner,
                                                      @RequestParam("token") final String token,
                                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                      @RequestParam("date") final LocalDateTime dateTime,
                                                      @Valid @RequestBody final CnProcess data) {
        return new ResponseEntity<>(
                cnOnPinService.createCnOnPin(cpId, previousStage, stage, owner, token, dateTime, data),
                HttpStatus.CREATED);
    }
}
