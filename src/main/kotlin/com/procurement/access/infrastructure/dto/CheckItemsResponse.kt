package com.procurement.access.infrastructure.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.CPVCode

data class CheckItemsResponse(
    @field:JsonProperty("mdmValidation") @param:JsonProperty("mdmValidation") val mdmValidation: Boolean,
    @field:JsonProperty("itemsAdd") @param:JsonProperty("itemsAdd") val itemsAdd: Boolean,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender? = null
) {

    companion object {
        fun resultUndefined(): CheckItemsResponse = CheckItemsResponse(mdmValidation = false, itemsAdd = false)
    }

    data class Tender(
        @field:JsonProperty("classification") @param:JsonProperty("classification") val classification: Classification
    ) {
        data class Classification(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: CPVCode
        )
    }
}
