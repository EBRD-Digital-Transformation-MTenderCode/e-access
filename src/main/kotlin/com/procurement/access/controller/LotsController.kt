package com.procurement.access.controller

import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.lots.CheckLotStatusRequestDto
import com.procurement.access.model.dto.lots.LotsRequestDto
import com.procurement.access.model.dto.ocds.TenderStatus
import com.procurement.access.model.dto.ocds.TenderStatusDetails
import com.procurement.access.service.LotsService
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
    fun getLots(@RequestParam("cpid") cpId: String,
                @RequestParam("stage") stage: String,
                @RequestParam("status") status: String): ResponseEntity<ResponseDto> {
        return ResponseEntity(
                lotsService.getLots(
                        cpId = cpId,
                        stage = stage,
                        status = TenderStatus.fromValue(status)),
                HttpStatus.OK)
    }

    @PostMapping("/updateStatusDetails")
    fun updateStatusDetails(@RequestParam("cpid") cpId: String,
                            @RequestParam("stage") stage: String,
                            @RequestParam("statusDetails") statusDetails: String,
                            @Valid @RequestBody data: LotsRequestDto): ResponseEntity<ResponseDto> {
        return ResponseEntity(
                lotsService.updateStatusDetails(
                        cpId = cpId,
                        stage = stage,
                        tenderStatusDetails = TenderStatusDetails.fromValue(statusDetails),
                        lotsDto = data),
                HttpStatus.OK)
    }

    @PostMapping("/updateStatusDetailsById")
    fun updateStatusDetailsById(@RequestParam("cpid") cpId: String,
                                @RequestParam("stage") stage: String,
                                @RequestParam("lotAwarded") lotAwarded: Boolean,
                                @RequestParam("lotId") lotId: String): ResponseEntity<ResponseDto> {
        return ResponseEntity(
                lotsService.updateStatusDetailsById(
                        cpId = cpId,
                        stage = stage,
                        lotId = lotId,
                        lotAwarded = lotAwarded),
                HttpStatus.OK)
    }

    @GetMapping("/checkStatusDetails")
    fun checkStatusDetails(@RequestParam("cpid") cpId: String,
                           @RequestParam("stage") stage: String): ResponseEntity<ResponseDto> {
        return ResponseEntity(
                lotsService.checkStatusDetails(
                        cpId = cpId,
                        stage = stage),
                HttpStatus.OK)
    }

    @PostMapping("/checkStatus")
    fun checkStatus(@RequestParam("cpid") cpId: String,
                    @RequestParam("stage") stage: String,
                    @Valid @RequestBody data: CheckLotStatusRequestDto): ResponseEntity<ResponseDto> {
        return ResponseEntity(
                lotsService.checkStatus(
                        cpId = cpId,
                        stage = stage,
                        lotDto = data),
                HttpStatus.OK)
    }

    @PostMapping("/updateLots")
    fun updateLots(@RequestParam("cpid") cpId: String,
                   @RequestParam("stage") stage: String,
                   @Valid @RequestBody data: LotsRequestDto): ResponseEntity<ResponseDto> {
        return ResponseEntity(
                lotsService.updateLots(
                        cpId = cpId,
                        stage = stage,
                        lotsDto = data),
                HttpStatus.OK)
    }

    @PostMapping("/updateLotsEv")
    fun updateLotsEv(@RequestParam("cpid") cpId: String,
                     @RequestParam("stage") stage: String,
                     @Valid @RequestBody data: LotsRequestDto): ResponseEntity<ResponseDto> {
        return ResponseEntity(
                lotsService.updateLotsEv(
                        cpId = cpId,
                        stage = stage,
                        lotsDto = data),
                HttpStatus.OK)
    }

}
