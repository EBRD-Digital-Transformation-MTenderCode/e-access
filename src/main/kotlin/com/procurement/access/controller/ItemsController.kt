package com.procurement.access.controller

import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.items.ItemsRq
import com.procurement.access.service.ItemsService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Validated
@RestController
@RequestMapping("/checkItems")
class ItemsController(private val itemsService: ItemsService) {

    @PostMapping
    fun checkItems(@RequestParam("country") country: String,
                   @RequestParam("pmd") pmd: String,
                   @Valid @RequestBody data: ItemsRq): ResponseEntity<ResponseDto> {
        return ResponseEntity(
                itemsService.checkItems(
                        country = country,
                        pmd = pmd,
                        itemsDto = data),
                HttpStatus.OK)
    }
}
