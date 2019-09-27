package com.procurement.access.infrastructure.bind.criteria

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.procurement.access.infrastructure.dto.cn.criteria.ExpectedValue
import com.procurement.access.infrastructure.dto.cn.criteria.MaxValue
import com.procurement.access.infrastructure.dto.cn.criteria.MinValue
import com.procurement.access.infrastructure.dto.cn.criteria.Period
import com.procurement.access.infrastructure.dto.cn.criteria.RangeValue
import com.procurement.access.infrastructure.dto.cn.criteria.Requirement
import com.procurement.access.infrastructure.dto.cn.criteria.RequirementDataType
import com.procurement.access.infrastructure.dto.cn.criteria.RequirementValue
import com.procurement.access.model.dto.databinding.JsonDateTimeDeserializer
import java.io.IOException

class RequirementDeserializer : JsonDeserializer<List<Requirement>>() {
    companion object {
        fun deserialize(requirements: ArrayNode): List<Requirement> {

            return requirements.map { requirement ->
                val id: String = requirement.get("id").asText()
                val title: String = requirement.get("title").asText()
                val dataType: RequirementDataType = RequirementDataType.fromString(requirement.get("dataType").asText())
                val description: String? = requirement.takeIf { it.has("description") }?.get("description")?.asText()
                val period: Period? = requirement.takeIf { it.has("period") }
                    ?.let {
                        val period = it.get("period")
                        val startDate = JsonDateTimeDeserializer.deserialize(period.get("startDate").asText())
                        val endDate = JsonDateTimeDeserializer.deserialize(period.get("endDate").asText())
                        Period(
                            startDate = startDate,
                            endDate = endDate
                        )
                    }

                Requirement(
                    id = id,
                    title = title,
                    description = description,
                    period = period,
                    dataType = dataType,
                    value = requirementValue(requirement)
                )
            }
        }

        private fun requirementValue(requirementNode: JsonNode): RequirementValue? {
            val dataType = RequirementDataType.fromString(requirementNode.get("dataType").asText())
            return if (isExpectedValue(requirementNode)) {
                when (dataType) {
                    RequirementDataType.BOOLEAN -> ExpectedValue.of(requirementNode.get("expectedValue").booleanValue())
                    RequirementDataType.STRING -> ExpectedValue.of(requirementNode.get("expectedValue").textValue())
                    RequirementDataType.NUMBER -> ExpectedValue.of(requirementNode.get("expectedValue").decimalValue())
                    RequirementDataType.INTEGER -> ExpectedValue.of(requirementNode.get("expectedValue").longValue())
                }
            } else if (isRange(requirementNode)) {
                when (dataType) {
                    RequirementDataType.NUMBER -> RangeValue.of(
                        requirementNode.get("minValue").decimalValue(),
                        requirementNode.get("maxValue").decimalValue()
                    )
                    RequirementDataType.INTEGER -> RangeValue.of(
                        requirementNode.get("minValue").decimalValue(),
                        requirementNode.get("maxValue").decimalValue()
                    )
                    RequirementDataType.BOOLEAN, RequirementDataType.STRING -> throw RuntimeException()
                }
            } else if (isOnlyMax(requirementNode)) {
                when (dataType) {
                    RequirementDataType.NUMBER -> MaxValue.of(requirementNode.get("maxValue").decimalValue())
                    RequirementDataType.INTEGER -> MaxValue.of(requirementNode.get("maxValue").longValue())
                    RequirementDataType.BOOLEAN, RequirementDataType.STRING -> throw RuntimeException()
                }
            } else if (isOnlyMin(requirementNode)) {
                when (dataType) {
                    RequirementDataType.NUMBER -> MinValue.of(requirementNode.get("minValue").decimalValue())
                    RequirementDataType.INTEGER -> MinValue.of(requirementNode.get("minValue").longValue())
                    RequirementDataType.BOOLEAN, RequirementDataType.STRING -> throw RuntimeException()
                }
            } else {
                null
            }
        }

        private fun isExpectedValue(requirementNode: JsonNode) = requirementNode.has("expectedValue")
            && (!requirementNode.has("minValue") || !requirementNode.has("maxValue"))

        private fun isRange(requirementNode: JsonNode) =
            requirementNode.has("minValue") && requirementNode.has("maxValue")

        private fun isOnlyMax(requirementNode: JsonNode) = requirementNode.has("maxValue")
        private fun isOnlyMin(requirementNode: JsonNode) = requirementNode.has("minValue")
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(
        jsonParser: JsonParser,
        deserializationContext: DeserializationContext
    ): List<Requirement> {
        val requirementNode = jsonParser.readValueAsTree<ArrayNode>()
        return deserialize(requirementNode)
    }
}
