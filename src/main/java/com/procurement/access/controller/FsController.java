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

    @PostMapping
    public ResponseEntity<ResponseDto> create(@RequestParam("owner") final String owner,
                                              @Valid @RequestBody final FsDto fsDto) {
        return new ResponseEntity<>(fsService.createFs(owner, fsDto), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<ResponseDto> update(@RequestParam("owner") final String owner,
                                              @RequestParam("identifier") final String identifier,
                                              @RequestParam("token") final String token,
                                              @Valid @RequestBody final FsDto fsDto) {
        return new ResponseEntity<>(fsService.updateFs(owner, identifier, token, fsDto), HttpStatus.OK);
    }

}
