package com.procurement.access.controller

import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.service.StageService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/newStage")
class StageController(private val stageService: StageService) {

//    @PostMapping
//    fun startNewStage(@RequestParam("cpid") cpId: String,
//                      @RequestParam("token") token: String,
//                      @RequestParam("previousStage") previousStage: String,
//                      @RequestParam("stage") newStage: String,
//                      @RequestParam("owner") owner: String): ResponseEntity<ResponseDto> {
//        return ResponseEntity(
//                stageService.startNewStage(
//                        cpId = cpId,
//                        token = token,
//                        previousStage = previousStage,
//                        newStage = newStage,
//                        owner = owner),
//                HttpStatus.OK)
//    }


}
