package com.procurement.access.infrastructure.bind.criteria

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.procurement.access.domain.model.requirement.ExpectedValue
import com.procurement.access.domain.model.requirement.MaxValue
import com.procurement.access.domain.model.requirement.MinValue
import com.procurement.access.domain.model.requirement.NoneValue
import com.procurement.access.domain.model.requirement.RangeValue
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.entity.TemplatesOfCriteriaForEvPanels
import java.io.IOException
import java.math.BigDecimal

class TemplateRequirementSerializer :
    JsonSerializer<List<TemplatesOfCriteriaForEvPanels.Criterion.RequirementGroup.Requirement>>() {

    companion object {

        fun serialize(requirements: List<TemplatesOfCriteriaForEvPanels.Criterion.RequirementGroup.Requirement>): ArrayNode {
            val serializedRequirements = JsonNodeFactory.withExactBigDecimals(true).arrayNode()
            requirements
                .map { requirement ->
                    JsonNodeFactory.withExactBigDecimals(true)
                        .objectNode()
                        .apply {
                            put("title", requirement.title)
                            put("dataType", requirement.dataType.toString())

                            when (requirement.value) {
                                is ExpectedValue.AsString -> put("expectedValue", requirement.value.value)
                                is ExpectedValue.AsBoolean -> put("expectedValue", requirement.value.value)
                                is ExpectedValue.AsNumber ->
                                    put("expectedValue", BigDecimal(requirement.value.toString()))
                                is ExpectedValue.AsInteger -> put("expectedValue", requirement.value.value)
                                is NoneValue -> Unit

                                is MinValue.AsNumber,
                                is MinValue.AsInteger,
                                is MaxValue.AsNumber,
                                is MaxValue.AsInteger,
                                is RangeValue.AsNumber,
                                is RangeValue.AsInteger -> throw ErrorException(
                                    error = ErrorType.INVALID_REQUIREMENT_VALUE,
                                    message = "Error of serialization. Unknown value in Requirement object. Allowed only 'expectedValue'."
                                )
                            }
                        }
                }
                .forEach { requirement -> serializedRequirements.add(requirement) }

            return serializedRequirements
        }
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(
        requirements: List<TemplatesOfCriteriaForEvPanels.Criterion.RequirementGroup.Requirement>,
        jsonGenerator: JsonGenerator,
        provider: SerializerProvider
    ) = jsonGenerator.writeTree(serialize(requirements))
}
