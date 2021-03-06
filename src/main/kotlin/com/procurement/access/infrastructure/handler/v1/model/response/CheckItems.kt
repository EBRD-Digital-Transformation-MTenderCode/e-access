package com.procurement.access.infrastructure.handler.v1.model.response

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude

data class CheckItemsRq @JsonCreator constructor(

        val items: List<ItemCheck>
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CheckItemsRs @JsonCreator constructor(

        val mdmValidation: Boolean,

        val itemsAdd: Boolean? = null,

        val tender: TenderCheck? = null
)

data class TenderCheck @JsonCreator constructor(

        var classification: ClassificationCheck
)

data class ItemCheck @JsonCreator constructor(

        val id: String,

        val classification: ClassificationCheck
)

data class ClassificationCheck @JsonCreator constructor(

        val id: String
)
