package com.procurement.access.controller

import com.procurement.access.model.dto.lots.LotsRequestDto
import com.procurement.access.model.dto.ocds.TenderStatus
import com.procurement.access.model.dto.ocds.TenderStatusDetails
import com.procurement.access.service.LotsService
import com.procurement.notice.model.bpe.ResponseDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Validated
@RestController
@RequestMapping("/lots")
class LotsController(private val lotsService: LotsService) {

    @GetMapping
    fun getLots(@RequestParam("identifier") cpId: String,
                @RequestParam("stage") stage: String,
                @RequestParam("status") status: String): ResponseEntity<ResponseDto<*>> {
        return ResponseEntity(
                lotsService.getLots(
                        cpId = cpId,
                        stage = stage,
                        status = TenderStatus.fromValue(status)),
                HttpStatus.OK)
    }

    @PostMapping("/updateStatus")
    fun updateStatus(@RequestParam("identifier") cpId: String,
                     @RequestParam("stage") stage: String,
                     @RequestParam("status") status: String,
                     @Valid @RequestBody data: LotsRequestDto): ResponseEntity<ResponseDto<*>> {
        return ResponseEntity(
                lotsService.updateStatus(
                        cpId = cpId,
                        stage = stage,
                        tenderStatus = TenderStatus.fromValue(status),
                        lotsDto = data),
                HttpStatus.OK)
    }

    @PostMapping("/updateStatusDetails")
    fun updateStatusDetails(@RequestParam("identifier") cpId: String,
                            @RequestParam("stage") stage: String,
                            @RequestParam("statusDetails") statusDetails: String,
                            @Valid @RequestBody data: LotsRequestDto): ResponseEntity<ResponseDto<*>> {
        return ResponseEntity(
                lotsService.updateStatusDetails(
                        cpId = cpId,
                        stage = stage,
                        tenderStatusDetails = TenderStatusDetails.fromValue(statusDetails),
                        lotsDto = data),
                HttpStatus.OK)
    }

    @PostMapping("/updateStatusDetailsById")
    fun updateStatusDetailsById(@RequestParam("identifier") cpId: String,
                                @RequestParam("stage") stage: String,
                                @RequestParam("statusDetails") statusDetails: String,
                                @RequestParam("lotId") lotId: String): ResponseEntity<ResponseDto<*>> {
        return ResponseEntity(
                lotsService.updateStatusDetailsById(
                        cpId = cpId,
                        stage = stage,
                        lotId = lotId,
                        statusDetails = TenderStatusDetails.fromValue(statusDetails)),
                HttpStatus.OK)
    }

    @GetMapping("/checkStatusDetails")
    fun checkStatusDetails(@RequestParam("identifier") cpId: String,
                           @RequestParam("stage") stage: String): ResponseEntity<ResponseDto<*>> {
        return ResponseEntity(
                lotsService.checkStatusDetails(
                        cpId = cpId,
                        stage = stage),
                HttpStatus.OK)
    }

    @PostMapping("/updateLots")
    fun updateLots(@RequestParam("identifier") cpId: String,
                   @RequestParam("stage") stage: String,
                   @Valid @RequestBody data: LotsRequestDto): ResponseEntity<ResponseDto<*>> {
        return ResponseEntity(
                lotsService.updateLots(
                        cpId = cpId,
                        stage = stage,
                        lotsDto = data),
                HttpStatus.OK)
    }

}
