package com.procurement.access.infrastructure.handler.v1.model.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.domain.model.coefficient.CoefficientRate
import com.procurement.access.domain.model.coefficient.CoefficientValue
import com.procurement.access.domain.model.enums.ConversionsRelatesTo
import com.procurement.access.domain.model.option.RelatedOption
import com.procurement.access.infrastructure.bind.coefficient.CoefficientRateDeserializer
import com.procurement.access.infrastructure.bind.coefficient.CoefficientRateSerializer
import com.procurement.access.infrastructure.bind.coefficient.value.CoefficientValueDeserializer
import com.procurement.access.infrastructure.bind.coefficient.value.CoefficientValueSerializer

class ConversionRequest(
    @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
    @field:JsonProperty("relatesTo") @param:JsonProperty("relatesTo") val relatesTo: ConversionsRelatesTo,
    @field:JsonProperty("relatedItem") @param:JsonProperty("relatedItem") val relatedItem: String,
    @field:JsonProperty("rationale") @param:JsonProperty("rationale") val rationale: String,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @field:JsonProperty("description") @param:JsonProperty("description") val description: String?,
    @field:JsonProperty("coefficients") @param:JsonProperty("coefficients") val coefficients: List<Coefficient>
) {

    data class Coefficient(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("relatedOption") @param:JsonProperty("relatedOption") val relatedOption: RelatedOption?,

        @JsonDeserialize(using = CoefficientValueDeserializer::class)
        @JsonSerialize(using = CoefficientValueSerializer::class)
        @field:JsonProperty("value") @param:JsonProperty("value") val value: CoefficientValue,

        @JsonDeserialize(using = CoefficientRateDeserializer::class)
        @JsonSerialize(using = CoefficientRateSerializer::class)
        @field:JsonProperty("coefficient") @param:JsonProperty("coefficient") val coefficient: CoefficientRate
    )
}
