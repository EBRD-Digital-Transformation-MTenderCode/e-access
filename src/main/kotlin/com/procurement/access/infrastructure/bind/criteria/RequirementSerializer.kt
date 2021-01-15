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
import com.procurement.access.domain.model.requirement.Requirement
import com.procurement.access.domain.util.extension.asString
import java.io.IOException
import java.math.BigDecimal

class RequirementSerializer : JsonSerializer<List<Requirement>>() {

    companion object {

        fun serialize(requirements: List<Requirement>): ArrayNode {
            fun BigDecimal.jsonFormat() = BigDecimal("%.3f".format(this))

            val serializedRequirements = JsonNodeFactory.withExactBigDecimals(true).arrayNode()

            requirements.map { requirement ->
                val requirementNode = JsonNodeFactory.withExactBigDecimals(true).objectNode()

                requirementNode.put("id", requirement.id)
                requirementNode.put("title", requirement.title)
                requirementNode.put("dataType", requirement.dataType.toString())

                requirement.description?.let { requirementNode.put("description", it) }
                requirement.status?.let { requirementNode.put("status", it.key) }
                requirement.datePublished?.let { requirementNode.put("datePublished", it.asString()) }

                requirement.period?.let {
                    requirementNode.putObject("period")
                        .put("startDate", it.startDate.asString())
                        .put("endDate", it.endDate.asString())
                }

                when (requirement.value) {
                    is ExpectedValue.AsString -> {
                        requirementNode.put("expectedValue", requirement.value.value)
                    }
                    is ExpectedValue.AsBoolean -> {
                        requirementNode.put("expectedValue", requirement.value.value)
                    }
                    is ExpectedValue.AsNumber -> {
                        requirementNode.put("expectedValue", BigDecimal(requirement.value.toString()))
                    }
                    is ExpectedValue.AsInteger -> {
                        requirementNode.put("expectedValue", requirement.value.value)
                    }
                    is RangeValue.AsNumber -> {
                        requirementNode.put("minValue", requirement.value.minValue.jsonFormat())
                        requirementNode.put("maxValue", requirement.value.maxValue.jsonFormat())
                    }
                    is RangeValue.AsInteger -> {
                        requirementNode.put("minValue", requirement.value.minValue)
                        requirementNode.put("maxValue", requirement.value.maxValue)
                    }
                    is MinValue.AsNumber -> {
                        requirementNode.put("minValue", BigDecimal(requirement.value.toString()))
                    }
                    is MinValue.AsInteger -> {
                        requirementNode.put("minValue", requirement.value.value)
                    }
                    is MaxValue.AsNumber -> {
                        requirementNode.put("maxValue", BigDecimal(requirement.value.toString()))
                    }
                    is MaxValue.AsInteger -> {
                        requirementNode.put("maxValue", requirement.value.value)
                    }
                    is NoneValue -> Unit
                }

                requirement.eligibleEvidences
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { eligibleEvidences ->
                        val array = requirementNode.putArray("eligibleEvidences")
                        eligibleEvidences.forEach { eligibleEvidence ->
                            array.addObject()
                                .apply {
                                    put("id", eligibleEvidence.id)
                                    put("title", eligibleEvidence.title)

                                    if (eligibleEvidence.description != null)
                                        put("description", eligibleEvidence.description)

                                    put("type", eligibleEvidence.type.key)

                                    if (eligibleEvidence.relatedDocument != null) {
                                        putObject("relatedDocument")
                                            .apply {
                                                put("id", eligibleEvidence.relatedDocument.id)
                                            }
                                    }
                                }
                        }
                    }

                requirementNode
            }.also { it.forEach { requirement -> serializedRequirements.add(requirement) } }

            return serializedRequirements
        }
    }

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(
        requirements: List<Requirement>,
        jsonGenerator: JsonGenerator,
        provider: SerializerProvider
    ) = jsonGenerator.writeTree(serialize(requirements))
}
