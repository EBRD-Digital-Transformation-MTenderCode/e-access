package com.procurement.access.controller

import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.bids.CheckBidRQDto
import com.procurement.access.service.BidsService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Validated
@RestController
@RequestMapping("/checkBid")
class BidsController(private val bidsService: BidsService) {

    @PostMapping
    fun checkBid(@RequestParam("cpid") cpId: String,
                 @RequestParam("stage") stage: String,
                 @Valid @RequestBody data: CheckBidRQDto): ResponseEntity<ResponseDto> {


        return ResponseEntity(
                bidsService.checkBid(
                        cpId = cpId,
                        stage = stage,
                        checkDto = data),
                HttpStatus.OK)
    }

}
