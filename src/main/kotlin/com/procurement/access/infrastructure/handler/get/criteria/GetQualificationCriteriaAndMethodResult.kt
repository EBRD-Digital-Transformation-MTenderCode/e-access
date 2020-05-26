package com.procurement.access.infrastructure.handler.get.criteria

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.domain.model.coefficient.CoefficientRate
import com.procurement.access.domain.model.coefficient.CoefficientValue
import com.procurement.access.domain.model.enums.ConversionsRelatesTo
import com.procurement.access.domain.model.enums.QualificationSystemMethod
import com.procurement.access.domain.model.enums.ReductionCriteria
import com.procurement.access.infrastructure.bind.coefficient.CoefficientRateDeserializer
import com.procurement.access.infrastructure.bind.coefficient.CoefficientRateSerializer
import com.procurement.access.infrastructure.bind.coefficient.value.CoefficientValueDeserializer
import com.procurement.access.infrastructure.bind.coefficient.value.CoefficientValueSerializer

data class GetQualificationCriteriaAndMethodResult(

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @field:JsonProperty("conversions") @param:JsonProperty("conversions") val conversions: List<Conversion>,

    @field:JsonProperty("qualificationSystemMethods") @param:JsonProperty("qualificationSystemMethods") val qualificationSystemMethods: List<QualificationSystemMethod>,
    @field:JsonProperty("reductionCriteria") @param:JsonProperty("reductionCriteria") val reductionCriteria: ReductionCriteria
) {
    data class Conversion(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
        @field:JsonProperty("relatesTo") @param:JsonProperty("relatesTo") val relatesTo: ConversionsRelatesTo,
        @field:JsonProperty("relatedItem") @param:JsonProperty("relatedItem") val relatedItem: String,
        @field:JsonProperty("rationale") @param:JsonProperty("rationale") val rationale: String,
        @field:JsonProperty("coefficients") @param:JsonProperty("coefficients") val coefficients: List<Coefficient>,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("description") @param:JsonProperty("description") val description: String?
    ) {
        data class Coefficient(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

            @JsonDeserialize(using = CoefficientValueDeserializer::class)
            @JsonSerialize(using = CoefficientValueSerializer::class)
            @field:JsonProperty("value") @param:JsonProperty("value") val value: CoefficientValue,

            @JsonDeserialize(using = CoefficientRateDeserializer::class)
            @JsonSerialize(using = CoefficientRateSerializer::class)
            @field:JsonProperty("coefficient") @param:JsonProperty("coefficient") val coefficient: CoefficientRate
        )
    }
}
