package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.service.StageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
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
    public ResponseEntity<ResponseDto> startNewStage(@RequestParam("identifier") final String cpId,
                                                     @RequestParam("token") final String token,
                                                     @RequestParam("previousStage") final String previousStage,
                                                     @RequestParam("stage") final String newStage,
                                                     @RequestParam("owner") final String owner) {
        return new ResponseEntity<>(
                stageService.startNewStage(cpId, token, previousStage, newStage, owner),
                HttpStatus.OK);
    }


}
