package com.procurement.access.infrastructure.bind.criteria

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.procurement.access.application.model.parseEnum
import com.procurement.access.domain.model.enums.RequirementDataType
import com.procurement.access.domain.model.enums.RequirementStatus
import com.procurement.access.domain.model.requirement.EligibleEvidence
import com.procurement.access.domain.model.requirement.EligibleEvidenceType
import com.procurement.access.domain.model.requirement.ExpectedValue
import com.procurement.access.domain.model.requirement.MaxValue
import com.procurement.access.domain.model.requirement.MinValue
import com.procurement.access.domain.model.requirement.NoneValue
import com.procurement.access.domain.model.requirement.RangeValue
import com.procurement.access.domain.model.requirement.Requirement
import com.procurement.access.domain.model.requirement.RequirementValue
import com.procurement.access.domain.util.extension.toLocalDateTime
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.handler.v1.model.request.document.RelatedDocumentRequest
import java.io.IOException
import java.math.BigDecimal
import java.time.LocalDateTime

class RequirementDeserializer : JsonDeserializer<List<Requirement>>() {
    companion object {

        private val allowedEligibleEvidenceTypes = EligibleEvidenceType.allowedElements.toSet()
        fun deserialize(requirements: ArrayNode): List<Requirement> {

            return requirements.map { requirement ->
                val id: String = requirement.get("id").asText()
                val title: String = requirement.get("title").asText()
                val description: String? = requirement.takeIf { it.has("description") }?.get("description")?.asText()

                val status: RequirementStatus? = requirement
                    .takeIf { it.has("status") }
                    ?.let { RequirementStatus.creator(it.get("status").asText()) }

                val datePublished: LocalDateTime? = requirement
                    .takeIf { it.has("datePublished") }
                    ?.let { dateNode -> dateNode.get("datePublished").asText().toLocalDateTime().orThrow { it.reason } }

                val dataType: RequirementDataType = RequirementDataType.creator(requirement.get("dataType").asText())
                val period: Requirement.Period? = requirement.takeIf { it.has("period") }
                    ?.let { node ->
                        val period = node.get("period")
                        val startDate = period.get("startDate").asText().toLocalDateTime().orThrow { it.reason }
                        val endDate = period.get("endDate").asText().toLocalDateTime().orThrow { it.reason }
                        Requirement.Period(
                            startDate = startDate,
                            endDate = endDate
                        )
                    }

                val eligibleEvidences: List<EligibleEvidence>? = requirement.get("eligibleEvidences")
                    ?.let { node ->
                        (node as ArrayNode).map { it.toEligibleEvidence() }
                    }

                Requirement(
                    id = id,
                    title = title,
                    description = description,
                    period = period,
                    dataType = dataType,
                    value = requirementValue(requirement),
                    eligibleEvidences = eligibleEvidences,
                    status = status,
                    datePublished = datePublished
                )
            }
        }

        private fun JsonNode.toEligibleEvidence(): EligibleEvidence {
            val id = get("id").asText()
            val title = get("title").asText()
            val description = get("description")?.asText()
            val type = get("type").asText()
                .let { value ->
                    parseEnum(
                        value = value,
                        allowedEnums = allowedEligibleEvidenceTypes,
                        attributeName = "eligibleEvidences",
                        target = EligibleEvidenceType
                    ).orThrow { IllegalArgumentException("Error of parsing element of 'EligibleEvidenceType' enum. Invalid value '$value'.") }
                }

            val relatedDocument = get("relatedDocument")
                ?.let { node ->
                    RelatedDocumentRequest(
                        id = node.get("id").asText()
                    )
                }

            return EligibleEvidence(
                id = id,
                title = title,
                description = description,
                type = type,
                relatedDocument = relatedDocument
            )
        }

        private fun requirementValue(requirementNode: JsonNode): RequirementValue {
            fun datatypeMismatchException(): Nothing = throw ErrorException(
                ErrorType.INVALID_REQUIREMENT_VALUE,
                message = "Requirement.dataType mismatch with datatype in expectedValue || minValue || maxValue."
            )

            val dataType = RequirementDataType.creator(requirementNode.get("dataType").asText())
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
                isRange(requirementNode) -> {
                    when (dataType) {
                        RequirementDataType.NUMBER ->
                            if (requirementNode.get("minValue").isBigDecimal && requirementNode.get("maxValue").isBigDecimal)
                                RangeValue.of(
                                    BigDecimal(requirementNode.get("minValue").asText()),
                                    BigDecimal(requirementNode.get("maxValue").asText())
                                )
                            else
                                datatypeMismatchException()

                        RequirementDataType.INTEGER ->
                            if (requirementNode.get("minValue").isBigInteger && requirementNode.get("maxValue").isBigInteger)
                                RangeValue.of(
                                    requirementNode.get("minValue").asLong(),
                                    requirementNode.get("maxValue").asLong()
                                )
                            else
                                datatypeMismatchException()

                        RequirementDataType.BOOLEAN,
                        RequirementDataType.STRING ->
                            throw ErrorException(
                                ErrorType.INVALID_REQUIREMENT_VALUE,
                                message = "Boolean or String datatype cannot have a range"
                            )
                    }
                }
                isOnlyMax(requirementNode) -> {
                    when (dataType) {
                        RequirementDataType.NUMBER ->
                            if (requirementNode.get("maxValue").isBigDecimal)
                                MaxValue.of(BigDecimal(requirementNode.get("maxValue").asText()))
                            else
                                datatypeMismatchException()
                        RequirementDataType.INTEGER ->
                            if (requirementNode.get("maxValue").isBigInteger)
                                MaxValue.of(requirementNode.get("maxValue").longValue())
                            else
                                datatypeMismatchException()
                        RequirementDataType.BOOLEAN,
                        RequirementDataType.STRING ->
                            throw ErrorException(
                                ErrorType.INVALID_REQUIREMENT_VALUE,
                                message = "Boolean or String datatype cannot have a max value"
                            )
                    }
                }
                isOnlyMin(requirementNode) -> {
                    when (dataType) {
                        RequirementDataType.NUMBER ->
                            if (requirementNode.get("minValue").isBigDecimal)
                                MinValue.of(BigDecimal(requirementNode.get("minValue").asText()))
                            else
                                datatypeMismatchException()

                        RequirementDataType.INTEGER ->
                            if (requirementNode.get("minValue").isBigInteger)
                                MinValue.of(requirementNode.get("minValue").longValue())
                            else
                                datatypeMismatchException()

                        RequirementDataType.BOOLEAN,
                        RequirementDataType.STRING ->
                            throw ErrorException(
                                ErrorType.INVALID_REQUIREMENT_VALUE,
                                message = "Boolean or String datatype cannot have a min value"
                            )
                    }
                }
                isNotBounded(requirementNode) -> NoneValue
                else -> {
                    throw ErrorException(
                        ErrorType.INVALID_REQUIREMENT_VALUE,
                        message = "Unknown value in Requirement object"
                    )
                }
            }
        }

        private fun isExpectedValue(requirementNode: JsonNode) = requirementNode.has("expectedValue")
            && !requirementNode.has("minValue") && !requirementNode.has("maxValue")

        private fun isRange(requirementNode: JsonNode) = requirementNode.has("minValue")
            && requirementNode.has("maxValue") && !requirementNode.has("expectedValue")

        private fun isOnlyMax(requirementNode: JsonNode) = requirementNode.has("maxValue")
            && !requirementNode.has("minValue") && !requirementNode.has("expectedValue")

        private fun isOnlyMin(requirementNode: JsonNode) = requirementNode.has("minValue")
            && !requirementNode.has("maxValue") && !requirementNode.has("expectedValue")

        private fun isNotBounded(requirementNode: JsonNode) = !requirementNode.has("expectedValue")
            && !requirementNode.has("minValue") && !requirementNode.has("maxValue")
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
