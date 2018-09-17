package com.procurement.access.controller

import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.pn.PnCreate
import com.procurement.access.model.dto.pn.PnUpdate
import com.procurement.access.service.PnService
import com.procurement.access.service.PnUpdateService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import javax.validation.Valid

@Validated
@RestController
@RequestMapping("/pn")
class PnController(private val pnService: PnService,
                   private val pnUpdateService: PnUpdateService) {

//    @PostMapping
//    fun createPn(@RequestParam("stage") stage: String,
//                 @RequestParam("country") country: String,
//                 @RequestParam("pmd") pmd: String,
//                 @RequestParam("owner") owner: String,
//                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
//                 @RequestParam("date") dateTime: LocalDateTime,
//                 @Valid @RequestBody data: PnCreate): ResponseEntity<ResponseDto> {
//
//        return ResponseEntity(
//                pnService.createPn(
//                        stage = stage,
//                        country = country,
//                        pmd = pmd,
//                        owner = owner,
//                        dateTime = dateTime,
//                        pnDto = data),
//                HttpStatus.CREATED)
//    }
//
//
//    @PutMapping
//    fun updatePn(@RequestParam("cpid") cpId: String,
//                 @RequestParam("stage") stage: String,
//                 @RequestParam("owner") owner: String,
//                 @RequestParam("token") token: String,
//                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
//                 @RequestParam("date") dateTime: LocalDateTime,
//                 @Valid @RequestBody data: PnUpdate): ResponseEntity<ResponseDto> {
//
//        return ResponseEntity(
//                pnUpdateService.updatePn(
//                        cpId = cpId,
//                        stage = stage,
//                        owner = owner,
//                        token = token,
//                        dateTime = dateTime,
//                        pnDto = data),
//                HttpStatus.CREATED)
//    }
}
