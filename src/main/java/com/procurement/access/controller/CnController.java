package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.tender.TenderProcessDto;
import com.procurement.access.service.TenderProcessService;
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

    private final TenderProcessService tenderProcessService;

    public CnController(final TenderProcessService tenderProcessService) {
        this.tenderProcessService = tenderProcessService;
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createCn(@RequestParam final String owner,
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                @RequestParam("date") final LocalDateTime dateTime,
                                                @Valid @RequestBody final TenderProcessDto dto) {
        return new ResponseEntity<>(tenderProcessService.createCn(owner, dateTime, dto), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<ResponseDto> updateCn(@RequestParam final String cpId,
                                                @RequestParam final String token,
                                                @RequestParam final String owner,
                                                @Valid @RequestBody final TenderProcessDto dto) {
        return new ResponseEntity<>(tenderProcessService.updateCn(owner, cpId, token, dto), HttpStatus.OK);
    }
}
