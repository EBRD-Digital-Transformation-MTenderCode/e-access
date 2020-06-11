package com.procurement.access.application.service.tender

import com.procurement.access.application.service.CheckResponsesData
import com.procurement.access.domain.model.enums.CriteriaRelatesToEnum
import com.procurement.access.domain.model.enums.RequirementDataType
import com.procurement.access.domain.model.requirement.response.RequirementRsValue
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.model.dto.databinding.JsonDateTimeSerializer
import java.time.Clock
import java.time.LocalDateTime

fun checkRequirementRelationRelevance(data: CheckResponsesData, criteria: List<CNEntity.Tender.Criteria>) {

    if (criteria.isEmpty() && data.bid.requirementResponses.isEmpty()) return

    val requirementsDb = criteria.asSequence()
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .toList()

    data.bid.requirementResponses
        .map { it.requirement }
        .forEach { requirementRequest ->
            requirementsDb.find { it.id == requirementRequest.id } ?: throw ErrorException(
                error = ErrorType.INVALID_REQUIREMENT_VALUE,
                message = "No requirement founded  by passed id=${requirementRequest.id}. "
            )
        }
}

fun checkAnswerCompleteness(data: CheckResponsesData, criteria: List<CNEntity.Tender.Criteria>) {

    if (criteria.isEmpty() && data.bid.requirementResponses.isEmpty()) return

    val lotRequirements = criteria.asSequence()
        .filter { it.relatedItem == data.bid.relatedLots[0] }
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .map { it.id }
        .toList()

    val itemsRequirements = data.items
        .asSequence()
        .map { item ->
            criteria.find { it.relatedItem == item.id }
        }
        .filterNotNull()
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .map { it.id }
        .toList()

    val requirementRequest = data.bid.requirementResponses
        .asSequence()
        .map { it.requirement }
        .map { it.id }
        .toList()

    val totalRequirements = lotRequirements + itemsRequirements
    val answered = (totalRequirements).intersect(requirementRequest)
    if (answered.size != totalRequirements.size)
        throw ErrorException(
            error = ErrorType.INVALID_REQUIREMENT_VALUE,
            message = "For lots and items founded ${totalRequirements.size} requirement in DB but answered for ${answered.size}. " +
                "Ignored requirements: ${totalRequirements.minus(answered)} "
        )

    val tenderRequirements = criteria.asSequence()
        .filter { it.relatesTo == CriteriaRelatesToEnum.TENDERER || it.relatesTo == null }
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .map { it.id }
        .toList()

    val answeredTender = (tenderRequirements).intersect(requirementRequest)
    if (answeredTender.size != tenderRequirements.size)
        throw ErrorException(
            error = ErrorType.INVALID_REQUIREMENT_VALUE,
            message = "For tenderer and tender founded ${tenderRequirements.size} requirement in DB but answered for ${answeredTender.size}. " +
                "Ignored requirements: ${tenderRequirements.minus(answeredTender)} "
        )

    if (requirementRequest.size != totalRequirements.size + tenderRequirements.size)
        throw ErrorException(
            error = ErrorType.INVALID_REQUIREMENT_VALUE,
            message = "Need to answer on ${totalRequirements.size + tenderRequirements.size} requirements, but answered on ${requirementRequest.size}. "
        )
}

fun checkAnsweredOnce(data: CheckResponsesData) {

    if (data.bid.requirementResponses.isEmpty()) return

    val requirementIds = data.bid.requirementResponses.asSequence()
        .map { it.requirement }
        .map { it.id }
        .toList()

    val uniqueAnswer = requirementIds.toSet()

    if (uniqueAnswer.size != requirementIds.size)
        throw throw ErrorException(
            error = ErrorType.INVALID_REQUIREMENT_VALUE,
            message = "Founded duplicated answer. Duplicated: ${requirementIds.groupBy { it }
                .filter { it.value.size > 1 }.keys}"
        )
}

fun checkDataTypeValue(data: CheckResponsesData, criteria: List<CNEntity.Tender.Criteria>) {
    fun dataTypeMismatchException(
        requirementResponseDatatype: RequirementRsValue,
        requirementDbDataType: RequirementDataType,
        requirementResponse: CheckResponsesData.Bid.RequirementResponse
    ): Nothing = throw ErrorException(
        error = ErrorType.INVALID_REQUIREMENT_VALUE,
        message = "RequirementResponse dataType= ${requirementResponseDatatype.javaClass}, " +
            "Requirement dataType (DB) = ${requirementDbDataType.key}. " +
            "ReqirementResponse.id=${requirementResponse.id}." +
            "ReqirementResponse.requirement.id=${requirementResponse.requirement.id}. "
    )

    if (criteria.isEmpty() && data.bid.requirementResponses.isEmpty()) return

    val requirements = criteria.asSequence()
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .map { requirement -> requirement.id to requirement }
        .toMap()

    data.bid.requirementResponses.forEach { requirementResponse ->
        val requirement = requirements[requirementResponse.requirement.id]
            ?: throw ErrorException(
                error = ErrorType.INVALID_REQUIREMENT_VALUE,
                message = "Cannot find requirement id DB by id=${requirementResponse.requirement.id}. "
            )

        when (requirementResponse.value) {
            is RequirementRsValue.AsString -> if (requirement.dataType != RequirementDataType.STRING)
                dataTypeMismatchException(
                    requirementResponseDatatype = requirementResponse.value,
                    requirementDbDataType = requirement.dataType,
                    requirementResponse = requirementResponse
                )
            is RequirementRsValue.AsBoolean -> if (requirement.dataType != RequirementDataType.BOOLEAN)
                dataTypeMismatchException(
                    requirementResponseDatatype = requirementResponse.value,
                    requirementDbDataType = requirement.dataType,
                    requirementResponse = requirementResponse
                )
            is RequirementRsValue.AsInteger -> if (requirement.dataType != RequirementDataType.INTEGER)
                dataTypeMismatchException(
                    requirementResponseDatatype = requirementResponse.value,
                    requirementDbDataType = requirement.dataType,
                    requirementResponse = requirementResponse
                )
            is RequirementRsValue.AsNumber -> if (requirement.dataType != RequirementDataType.NUMBER)
                dataTypeMismatchException(
                    requirementResponseDatatype = requirementResponse.value,
                    requirementDbDataType = requirement.dataType,
                    requirementResponse = requirementResponse
                )
        }
    }
}

fun checkPeriod(data: CheckResponsesData) {
    fun invalidPeriodExpection(endDate: LocalDateTime, currentTime: LocalDateTime): Nothing = throw ErrorException(
        error = ErrorType.INVALID_PERIOD_VALUE,
        message = "Period.endDate specified in RequirementResponse cannot be greater or equals to current time. " +
            "EndDate = ${JsonDateTimeSerializer.serialize(endDate)}, Current time = ${JsonDateTimeSerializer.serialize(
                currentTime
            )}"
    )

    fun invalidPeriodExpection(period: CheckResponsesData.Bid.RequirementResponse.Period): Nothing =
        throw ErrorException(
            error = ErrorType.INVALID_PERIOD_VALUE,
            message = "Period.startDate specified in RequirementResponse cannot be greater or equals to period.endDate. " +
                "StratDate = ${JsonDateTimeSerializer.serialize(period.startDate)}, " +
                "EndDate = ${JsonDateTimeSerializer.serialize(period.endDate)}"
        )

    if (data.bid.requirementResponses.isEmpty()) return

    data.bid.requirementResponses
        .mapNotNull { it.period }
        .forEach {
            val currentTime = LocalDateTime.now(Clock.systemUTC())
            if (it.endDate >= currentTime) invalidPeriodExpection(endDate = it.endDate, currentTime = currentTime)
            if (it.startDate >= it.endDate) invalidPeriodExpection(period = it)
        }
}

fun checkIdsUniqueness(data: CheckResponsesData, criteria: List<CNEntity.Tender.Criteria>) {
    if (data.bid.requirementResponses.isEmpty()) return

    val requirementResponseIds = data.bid.requirementResponses.map { it.id }
    val requirementResponseUniqueIds = requirementResponseIds.toSet()
    if (requirementResponseIds.size != requirementResponseUniqueIds.size) throw ErrorException(
        error = ErrorType.INVALID_REQUIREMENT_VALUE,
        message = "Id in RequirementResponse object must be unique. All ids: ${requirementResponseIds}. " +
            "Duplicated ids: ${requirementResponseIds.groupBy { it }.filter { it.value.size > 1 }.keys}"
    )
}
