package com.procurement.access.infrastructure.bind.criteria

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.procurement.access.domain.model.enums.RequirementDataType
import com.procurement.access.domain.model.requirement.ExpectedValue
import com.procurement.access.domain.model.requirement.NoneValue
import com.procurement.access.domain.model.requirement.RequirementValue
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.entity.TemplatesOfCriteriaForEvPanels
import java.io.IOException
import java.math.BigDecimal

class TemplateRequirementDeserializer :
    JsonDeserializer<List<TemplatesOfCriteriaForEvPanels.Criterion.RequirementGroup.Requirement>>() {

    companion object {

        fun deserialize(requirements: ArrayNode): List<TemplatesOfCriteriaForEvPanels.Criterion.RequirementGroup.Requirement> =
            requirements.map { requirement ->
                val title: String = requirement.get("title").asText()
                val dataType: RequirementDataType = RequirementDataType.creator(requirement.get("dataType").asText())
                TemplatesOfCriteriaForEvPanels.Criterion.RequirementGroup.Requirement(
                    title = title,
                    dataType = dataType,
                    value = requirementValue(requirement, dataType)
                )
            }

        private fun requirementValue(requirementNode: JsonNode, dataType: RequirementDataType): RequirementValue {
            fun datatypeMismatchException(): Nothing = throw ErrorException(
                ErrorType.INVALID_REQUIREMENT_VALUE,
                message = "Requirement.dataType mismatch with datatype in expectedValue."
            )

            return when {
                isExpectedValue(requirementNode) -> {
                    when (dataType) {
                        RequirementDataType.BOOLEAN ->
                            if (requirementNode.get("expectedValue").isBoolean)
                                ExpectedValue.of(requirementNode.get("expectedValue").booleanValue())
                            else
                                datatypeMismatchException()

                        RequirementDataType.STRING ->
                            if (requirementNode.get("expectedValue").isTextual)
                                ExpectedValue.of(requirementNode.get("expectedValue").textValue())
                            else
                                datatypeMismatchException()

                        RequirementDataType.NUMBER ->
                            if (requirementNode.get("expectedValue").isBigDecimal)
                                ExpectedValue.of(BigDecimal(requirementNode.get("expectedValue").asText()))
                            else
                                datatypeMismatchException()

                        RequirementDataType.INTEGER ->
                            if (requirementNode.get("expectedValue").isBigInteger)
                                ExpectedValue.of(requirementNode.get("expectedValue").longValue())
                            else
                                datatypeMismatchException()
                    }
                }
                isNotBounded(requirementNode) -> NoneValue
                else -> throw ErrorException(
                    error = ErrorType.INVALID_REQUIREMENT_VALUE,
                    message = "Error of deserialization. Unknown value in Requirement object. Allowed only 'expectedValue'."
                )
            }
        }

        private fun isExpectedValue(requirementNode: JsonNode) = requirementNode.has("expectedValue")
            && !requirementNode.has("minValue") && !requirementNode.has("maxValue")

        private fun isNotBounded(requirementNode: JsonNode) = !requirementNode.has("expectedValue")
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(
        jsonParser: JsonParser,
        deserializationContext: DeserializationContext
    ): List<TemplatesOfCriteriaForEvPanels.Criterion.RequirementGroup.Requirement> =
        deserialize(jsonParser.readValueAsTree())
}
