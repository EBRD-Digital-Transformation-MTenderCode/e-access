package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.tender.CnDto;
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
    public ResponseEntity<ResponseDto> createCn(@RequestParam final String owner,
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                @RequestParam("date") final LocalDateTime dateTime,
                                                @Valid @RequestBody final CnDto dto) {
        return new ResponseEntity<>(cnService.createCn(owner, dateTime, dto), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<ResponseDto> updateCn(@RequestParam final String cpId,
                                                @RequestParam final String token,
                                                @RequestParam final String owner,
                                                @Valid @RequestBody final CnDto dto) {
        return new ResponseEntity<>(cnService.updateCn(owner, cpId, token, dto), HttpStatus.OK);
    }
}
