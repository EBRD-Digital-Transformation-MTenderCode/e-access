package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.tender.CnDto;
import com.procurement.access.service.CnService;
import com.procurement.access.service.StageService;
import java.time.LocalDateTime;
import javax.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/newStage")
public class StageController {

    private final StageService stageService;

    public StageController(final StageService stageService) {
        this.stageService = stageService;
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createCn(@RequestParam(value = "identifier") final String cpId,
                                                @RequestParam(value = "token") final String token,
                                                @RequestParam(value = "previousstage") final String previousStage,
                                                @RequestParam(value = "stage") final String stage,
                                                @RequestParam(value = "owner") final String owner) {
        return new ResponseEntity<>(stageService.startNewStage(cpId,token,previousStage,stage,owner), HttpStatus.OK);
    }


}
