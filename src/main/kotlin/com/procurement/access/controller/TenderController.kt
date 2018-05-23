package com.procurement.access.controller

import com.procurement.access.model.dto.ocds.TenderStatus
import com.procurement.access.model.dto.ocds.TenderStatusDetails
import com.procurement.access.service.TenderService
import com.procurement.notice.model.bpe.ResponseDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/tender")
class TenderController(private val tenderService: TenderService) {

    @PostMapping("/updateStatus")
    fun updateStatus(@RequestParam("identifier") cpId: String,
                     @RequestParam("stage") stage: String,
                     @RequestParam("status") status: String): ResponseEntity<ResponseDto<*>> {
        return ResponseEntity(
                tenderService.updateStatus(
                        cpId = cpId,
                        stage = stage,
                        status = TenderStatus.fromValue(status)),
                HttpStatus.OK)
    }

    @PostMapping("/updateStatusDetails")
    fun updateStatusDetails(@RequestParam("identifier") cpId: String,
                            @RequestParam("stage") stage: String,
                            @RequestParam("statusDetails") statusDetails: String): ResponseEntity<ResponseDto<*>> {
        return ResponseEntity(
                tenderService.updateStatusDetails(
                        cpId = cpId,
                        stage = stage,
                        statusDetails = TenderStatusDetails.fromValue(statusDetails)),
                HttpStatus.OK)
    }

    @PostMapping("/setSuspended")
    fun setSuspended(@RequestParam("identifier") cpId: String,
                     @RequestParam("stage") stage: String,
                     @RequestParam("suspended") suspended: Boolean?): ResponseEntity<ResponseDto<*>> {
        return ResponseEntity(
                tenderService.setSuspended(
                        cpId = cpId,
                        stage = stage,
                        suspended = suspended),
                HttpStatus.OK)
    }

    @PostMapping("/setUnsuccessful")
    fun setUnsuccessful(@RequestParam("identifier") cpId: String,
                        @RequestParam("stage") stage: String): ResponseEntity<ResponseDto<*>> {
        return ResponseEntity(
                tenderService.setUnsuccessful(
                        cpId = cpId,
                        stage = stage),
                HttpStatus.OK)
    }
}
