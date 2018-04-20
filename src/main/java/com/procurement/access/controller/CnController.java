package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.cn.CnProcess;
import com.procurement.access.service.CnService;
import java.time.LocalDateTime;
import javax.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/cn")
public class CnController {

    private final CnService cnService;

    public CnController(final CnService cnService) {
        this.cnService = cnService;
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createCn(@RequestParam("stage") final String stage,
                                                @RequestParam("country") final String country,
                                                @RequestParam(value = "pmd", required = false) final String pmd,
                                                @RequestParam("owner") final String owner,
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                @RequestParam("date") final LocalDateTime dateTime,
                                                @Valid @RequestBody final CnProcess data) {
        return new ResponseEntity<>(
                cnService.createCn(stage, country, owner, dateTime, data),
                HttpStatus.CREATED);
    }
}
