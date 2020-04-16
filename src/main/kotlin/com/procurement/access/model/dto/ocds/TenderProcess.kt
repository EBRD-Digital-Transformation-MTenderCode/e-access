package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TenderProcess @JsonCreator constructor(

        val ocid: String?,

//        var token: String?,

//        @field:JsonDeserialize(using = BooleansDeserializer::class)
//        @get:JsonProperty("isLotsChanged")
//        var isLotsChanged: Boolean? = null,

        var amendment: Amendment? = null,

        val planning: Planning,

        val tender: Tender

)

data class Amendment @JsonCreator constructor(

        val relatedLots: Set<String>
)
