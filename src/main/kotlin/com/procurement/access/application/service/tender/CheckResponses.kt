package com.procurement.access.application.service.tender

import com.procurement.access.application.service.CheckResponsesData
import com.procurement.access.domain.model.enums.CriteriaRelatesToEnum
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.domain.model.enums.RequirementDataType
import com.procurement.access.domain.model.requirement.response.RequirementRsValue
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.model.dto.databinding.JsonDateTimeSerializer
import java.time.Clock
import java.time.LocalDateTime

fun checkRequirementRelationRelevance(data: CheckResponsesData, criteria: List<CNEntity.Tender.Criteria>) {
    val requirementIds = criteria.asSequence()
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .map { it.id }
        .toSet()

    data.bid.requirementResponses
        .asSequence()
        .map { it.requirement }
        .forEach { requirement ->
            if (requirement.id !in requirementIds)
                throw ErrorException(
                    error = ErrorType.INVALID_REQUIREMENT_VALUE,
                    message = "No requirement founded by id: '${requirement.id}'."
                )
        }
}

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

    val requirementRequest = data.bid.requirementResponses.map { it.requirement.id }

    val totalRequirements = lotRequirements + itemsRequirements
    val answered = (totalRequirements).intersect(requirementRequest)
    if (answered.size < totalRequirements.size)
        throw ErrorException(
            error = ErrorType.INVALID_REQUIREMENT_VALUE,
            message = "For lots and items founded ${totalRequirements.size} requirement in DB but answered for ${answered.size}. " +
                "Ignored requirements: ${totalRequirements.minus(answered)} "
        )
    if (answered.size > totalRequirements.size)
        throw ErrorException(
            error = ErrorType.INVALID_REQUIREMENT_VALUE,
            message = "For lots and items founded ${totalRequirements.size} requirement in DB but answered for ${answered.size}. " +
                "Unnecessary requirements: ${answered.minus(totalRequirements)} "
        )
}

fun checkAnswerByTenderAndTendererRequirements(
    data: CheckResponsesData,
    criteria: List<CNEntity.Tender.Criteria>,
    pmd: ProcurementMethod
) = when (pmd) {
    ProcurementMethod.OT, ProcurementMethod.TEST_OT,
    ProcurementMethod.SV, ProcurementMethod.TEST_SV,
    ProcurementMethod.MV, ProcurementMethod.TEST_MV ->
        //FR.COM-1.16.4
        checkAnswerByTenderAndTendererRequirements(data, criteria)
    ProcurementMethod.GPA, ProcurementMethod.TEST_GPA -> {
        //FR.COM-1.16.10
        checkAnswerByTenderRequirementsGpa(data, criteria)
        //FR.COM-1.16.11
        checkAnswerByTendererRequirementsGpa(data, criteria)
    }
    ProcurementMethod.DA, ProcurementMethod.TEST_DA,
    ProcurementMethod.NP, ProcurementMethod.TEST_NP,
    ProcurementMethod.OP, ProcurementMethod.TEST_OP,
    ProcurementMethod.RT, ProcurementMethod.TEST_RT,
    ProcurementMethod.FA, ProcurementMethod.TEST_FA,
    ProcurementMethod.CD, ProcurementMethod.TEST_CD,
    ProcurementMethod.DC, ProcurementMethod.TEST_DC,
    ProcurementMethod.CF, ProcurementMethod.TEST_CF,
    ProcurementMethod.OF, ProcurementMethod.TEST_OF,
    ProcurementMethod.IP, ProcurementMethod.TEST_IP -> Unit
}

private fun checkAnswerByTenderAndTendererRequirements(
    data: CheckResponsesData,
    criteria: List<CNEntity.Tender.Criteria>
) {
    val requirementsReceived = data.bid.requirementResponses.map { it.requirement.id }

    val tenderRequirements = criteria.asSequence()
        .filter { it.relatesTo == CriteriaRelatesToEnum.TENDERER || it.relatesTo == null }
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .map { it.id }
        .toList()

    val answeredTender = (tenderRequirements).intersect(requirementsReceived)
    if (answeredTender.size != tenderRequirements.size)
        throw ErrorException(
            error = ErrorType.INVALID_SUITE_OF_REQUIREMENTS,
            message = "Found ${tenderRequirements.size} requirements in DB for tender and tenderer but received answers for ${answeredTender.size}. " +
                "Ignored requirements: ${tenderRequirements.minus(answeredTender)} "
        )
}

private fun checkAnswerByTenderRequirementsGpa(
    data: CheckResponsesData,
    criteria: List<CNEntity.Tender.Criteria>
) {
    val requirementsReceived = data.bid.requirementResponses.map { it.requirement.id }

    val tenderRequirements = criteria.asSequence()
        .filter { it.relatesTo == null }
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .map { it.id }
        .toList()

    val answeredTender = (tenderRequirements).intersect(requirementsReceived)
    if (answeredTender.size != tenderRequirements.size)
        throw ErrorException(
            error = ErrorType.INVALID_SUITE_OF_REQUIREMENTS,
            message = "Found ${tenderRequirements.size} requirements in DB for tender but received answers for ${answeredTender.size}. " +
                "Ignored requirements: ${tenderRequirements.minus(answeredTender)} "
        )
}

private fun checkAnswerByTendererRequirementsGpa(
    data: CheckResponsesData,
    criteria: List<CNEntity.Tender.Criteria>
) {
    val requirementsReceived = data.bid.requirementResponses.map { it.requirement.id }

    val tendererRequirements = criteria.asSequence()
        .filter { it.relatesTo == CriteriaRelatesToEnum.TENDERER }
        .flatMap { it.requirementGroups.asSequence() }
        .flatMap { it.requirements.asSequence() }
        .map { it.id }
        .toList()

    val answeredTenderer = (tendererRequirements).intersect(requirementsReceived)
    if (answeredTenderer.isNotEmpty())
        throw ErrorException(
            error = ErrorType.INVALID_SUITE_OF_REQUIREMENTS,
            message = "Redundant requirement responses has been provided for tenderer requirement(s) ${answeredTenderer.joinToString()}. "
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
