package com.procurement.access.controller

import com.procurement.access.model.bpe.CommandMessage
import com.procurement.access.model.bpe.CommandType
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.service.ValidationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/command")
class CommandController(private val validationService: ValidationService) {

    @PostMapping
    fun command(@RequestBody commandMessage: CommandMessage): ResponseEntity<ResponseDto> {
        return ResponseEntity(execute(commandMessage), HttpStatus.OK)
    }

    fun execute(cm: CommandMessage): ResponseDto {
        return when (cm.command) {
            CommandType.CHECK_BID -> validationService.checkBid(cm)
            CommandType.CHECK_ITEMS -> validationService.checkItems(cm)
        }
    }
}



