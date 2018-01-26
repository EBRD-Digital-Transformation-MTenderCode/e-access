package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.cn.CnDto;
import com.procurement.access.service.CnService;
import javax.validation.Valid;
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
    public ResponseEntity<ResponseDto> create(@RequestParam("owner") final String owner,
                                              @Valid @RequestBody final CnDto cnDto) {
        return new ResponseEntity<>(cnService.createCn(owner, cnDto), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<ResponseDto> update(@RequestParam("owner") final String owner,
                                              @RequestParam("identifier") final String identifier,
                                              @RequestParam("token") final String token,
                                              @Valid @RequestBody final CnDto cnDto) {
        return new ResponseEntity<>(cnService.updateCn(owner, identifier, token, cnDto), HttpStatus.OK);
    }
}
