package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.fs.FsDto;
import com.procurement.access.service.FsService;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/fs")
public class FsController {

    private final FsService fsService;

    public FsController(final FsService fsService) {
        this.fsService = fsService;
    }

    @PostMapping(value = "/create")
    public ResponseEntity<ResponseDto> create(@RequestParam("country") final String country,
                                              @RequestParam("pmd") final String pmd,
                                              @RequestParam("stage") final String stage,
                                              @RequestParam("owner") final String owner,
                                              @Valid @RequestBody final FsDto fsDto) {
        return new ResponseEntity<>(fsService.createFs(country, pmd, stage, owner, fsDto), HttpStatus.CREATED);
    }

    @PostMapping(value = "/update")
    public ResponseEntity<ResponseDto> update(@RequestParam("country") final String country,
                                              @RequestParam("pmd") final String pmd,
                                              @RequestParam("stage") final String stage,
                                              @RequestParam("owner") final String owner,
                                              @Valid @RequestBody final FsDto fsDto) {
        return new ResponseEntity<>(fsService.updateFs(fsDto), HttpStatus.OK);
    }

}
