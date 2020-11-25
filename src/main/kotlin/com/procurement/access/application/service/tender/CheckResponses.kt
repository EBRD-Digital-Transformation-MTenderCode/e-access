package com.procurement.access.application.service.tender

import com.procurement.access.application.service.CheckResponsesData
import com.procurement.access.domain.model.enums.CriteriaRelatesToEnum
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.enums.RequirementDataType
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.model.requirement.response.RequirementRsValue
import com.procurement.access.domain.util.extension.getMissingElements
import com.procurement.access.domain.util.extension.getUnknownElements
import com.procurement.access.domain.util.extension.toSetBy
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.model.dto.databinding.JsonDateTimeSerializer
import java.time.Clock
import java.time.LocalDateTime

fun checkProcuringEntityNotAnswered(data: CheckResponsesData, criteria: List<CNEntity.Tender.Criteria>) {
    val requirementsById = criteria.asSequence()
        .filter { it.source == CriteriaSource.PROCURING_ENTITY }
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .associateBy { it.id }

    data.bid.requirementResponses
        .asSequence()
        .map { it.requirement }
        .forEach { requirement ->
            if (requirement.id in requirementsById)
                throw ErrorException(
                    error = ErrorType.INVALID_REQUIREMENT_VALUE,
                    message = "Must not answer on requirement (id='${requirement.id}') for ${CriteriaSource.PROCURING_ENTITY}."
                )
        }
}

fun checkAnswerByLotRequirements(
    data: CheckResponsesData,
    criteria: List<CNEntity.Tender.Criteria>,
    items: List<CNEntity.Tender.Item>
) {

    val lotRequirements = criteria.asSequence()
        .filter { it.relatedItem == data.bid.relatedLots[0] }
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .map { it.id }
        .toList()

    val itemsRequirements = items
        .asSequence()
        .filter { it.relatedLot == data.bid.relatedLots[0] }
        .map { item -> criteria.filter { it.relatedItem == item.id } }
        .flatten()
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .map { it.id }
        .toList()

    val requestRequirements = data.bid.requirementResponses.map { it.requirement.id }
    val totalRequirements = lotRequirements + itemsRequirements


    val nonBiddedLotsRequirements  = criteria.asSequence()
        .filter { it.relatesTo == CriteriaRelatesToEnum.LOT }
        .filter { it.relatedItem != data.bid.relatedLots[0] }
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .toList()

    val nonBiddedItemsRequirements  = items.asSequence()
        .filter { it.relatedLot != data.bid.relatedLots[0] }
        .map { item -> criteria.filter { it.relatedItem == item.id } }
        .flatten()
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .toList()

    val nonTargetRequirementsIds = (nonBiddedLotsRequirements + nonBiddedItemsRequirements).toSetBy { it.id }

    val unknownRequirements = getUnknownElements(received = requestRequirements, known = totalRequirements)

    val redundantRequirements = unknownRequirements.intersect(nonTargetRequirementsIds)
    if (redundantRequirements.isNotEmpty())
        throw ErrorException(
            error = ErrorType.INVALID_REQUIREMENT_VALUE,
            message = "Received redundant requirements: '${unknownRequirements.joinToString()}'"
        )


    val missingRequirements = getMissingElements(received = requestRequirements, known = totalRequirements)
    if (missingRequirements.isNotEmpty())
        throw ErrorException(
            error = ErrorType.INVALID_REQUIREMENT_VALUE,
            message = "Missing expected requirements: '${missingRequirements.joinToString()}'"
        )
}

fun checkResponsesCompleteness(criteria: List<CNEntity.Tender.Criteria>, responses: CheckResponsesData, stage: Stage) {
    val receivedItems = responses.items.map { it.id }
    val biddedLots = responses.bid.relatedLots

    val criteriaToTender = criteria.filter { it.relatesTo == null }
    val criteriaToTenderer = criteria.filter { it.relatesTo == CriteriaRelatesToEnum.TENDERER }
    val criteriaToLot = criteria.filter { it.relatedItem in biddedLots }
    val criteriaToItem = criteria.filter { it.relatedItem in receivedItems }

    when(stage) {
        Stage.EV -> {
            val criteriaPackage = (criteriaToTender + criteriaToTenderer + criteriaToLot + criteriaToItem)

            checkResponsesRelationToOneGroup(criteriaPackage, responses)
            checkAnsweredOnlyExpectedRequirement(criteriaPackage, responses)
        }
        Stage.TP -> {
            val criteriaPackage = (criteriaToTender + criteriaToLot + criteriaToItem)

            checkResponsesRelationToOneGroup(criteriaPackage, responses)
            checkAnsweredOnlyExpectedRequirement(criteriaPackage, responses)
        }
        Stage.AC,
        Stage.AP,
        Stage.EI,
        Stage.FE,
        Stage.FS,
        Stage.NP,
        Stage.PC,
        Stage.PN -> Unit
    }
}

fun checkResponsesRelationToOneGroup(criteria: List<CNEntity.Tender.Criteria>, responses: CheckResponsesData) {
    criteria.forEach { criterion ->
        val receivedResponsesIds = responses.bid.requirementResponses.map { it.requirement.id }

        val usedGroups = criterion.requirementGroups
            .associate { it.id to it.requirements.map { it.id } }
            .filter { (_, requirements) -> receivedResponsesIds.any { it in requirements } }

        val answeredGroups = usedGroups.count()
        when {
            answeredGroups == 0 ->
                throw ErrorException(
                error = ErrorType.INVALID_REQUIREMENT_RESPONSE,
                message = "Need answer on the next criterion: ${criterion.id}."
            )

            answeredGroups == 1 -> {
                val storedRequirements = usedGroups.values.flatten()
                if (!receivedResponsesIds.containsAll(storedRequirements))
                    throw ErrorException(
                        error = ErrorType.INVALID_REQUIREMENT_RESPONSE,
                        message = "Need answer on all requirement in specified requirement group '${usedGroups.keys}'."
                    )
            }
            else ->
                throw ErrorException(
                    error = ErrorType.INVALID_REQUIREMENT_RESPONSE,
                    message = "Requirements responses relates to more than one requirement group in criteria '${criterion.id}'."
                )
        }
    }
}

fun checkAnsweredOnlyExpectedRequirement(criteria: List<CNEntity.Tender.Criteria>, responses: CheckResponsesData) {
    val storedRequirements = criteria.asSequence()
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .map { it.id }
        .toList()

    val receivedRequirementResponses = responses.bid.requirementResponses.map { it.requirement.id }

    val redundantAnswers = receivedRequirementResponses - storedRequirements
    if (redundantAnswers.isNotEmpty())
        throw ErrorException(
            error = ErrorType.INVALID_REQUIREMENT_RESPONSE,
            message = "Redundant responses for requirements. Requirement ids: ${redundantAnswers}."
        )
}

fun checkAnsweredOnce(data: CheckResponsesData) {
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

    val requirements = criteria.asSequence()
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .map { requirement -> requirement.id to requirement }
        .toMap()

    data.bid.requirementResponses
        .forEach { requirementResponse ->
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
    fun invalidPeriodException(endDate: LocalDateTime, currentTime: LocalDateTime): Nothing =
        throw ErrorException(
            error = ErrorType.INVALID_PERIOD_VALUE,
            message = "Period.endDate specified in RequirementResponse cannot be greater or equals to current time. " +
                "EndDate = ${JsonDateTimeSerializer.serialize(endDate)}, Current time = ${JsonDateTimeSerializer.serialize(
                    currentTime
                )}"
        )

    fun invalidPeriodException(period: CheckResponsesData.Bid.RequirementResponse.Period): Nothing =
        throw ErrorException(
            error = ErrorType.INVALID_PERIOD_VALUE,
            message = "Period.startDate specified in RequirementResponse cannot be greater or equals to period.endDate. " +
                "StratDate = ${JsonDateTimeSerializer.serialize(period.startDate)}, " +
                "EndDate = ${JsonDateTimeSerializer.serialize(period.endDate)}"
        )

    data.bid.requirementResponses
        .mapNotNull { it.period }
        .forEach {
            val currentTime = LocalDateTime.now(Clock.systemUTC())
            if (it.endDate >= currentTime) invalidPeriodException(endDate = it.endDate, currentTime = currentTime)
            if (it.startDate >= it.endDate) invalidPeriodException(period = it)
        }
}

fun checkIdsUniqueness(data: CheckResponsesData) {
    val requirementResponseIds = data.bid.requirementResponses.map { it.id }
    val requirementResponseUniqueIds = requirementResponseIds.toSet()
    if (requirementResponseIds.size != requirementResponseUniqueIds.size) throw ErrorException(
        error = ErrorType.INVALID_REQUIREMENT_VALUE,
        message = "Id in RequirementResponse object must be unique. All ids: ${requirementResponseIds}. " +
            "Duplicated ids: ${requirementResponseIds.groupBy { it }.filter { it.value.size > 1 }.keys}"
    )
}
