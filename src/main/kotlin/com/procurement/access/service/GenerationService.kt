package com.procurement.access.service

import com.datastax.driver.core.utils.UUIDs
import com.procurement.access.application.model.Mode
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.domain.util.extension.nowDefaultUTC
import com.procurement.access.domain.util.extension.toMilliseconds
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.dto.ocds.OrganizationReference
import org.springframework.stereotype.Service
import java.util.*

@Service
class GenerationService {

    fun getCpId(country: String, mode: Mode): String = mode.prefix + "-" + country + "-" + nowDefaultUTC().toMilliseconds()

    fun generatePermanentTenderId(): String {
        return UUID.randomUUID().toString()
    }

    fun generateRandomUUID(): UUID {
        return UUIDs.random()
    }

    fun generateTimeBasedUUID(): UUID {
        return UUIDs.timeBased()
    }

    fun getRandomUUID(): String {
        return generateRandomUUID().toString()
    }

    fun getTimeBasedUUID(): String {
        return generateTimeBasedUUID().toString()
    }

    fun generateToken(): UUID {
        return UUID.randomUUID()
    }

    fun generateOrganizationId(identifierScheme: String, identifierId: String): String {
        return "$identifierScheme-$identifierId"
    }

    fun generatePermanentLotId(): String {
        return UUID.randomUUID().toString()
    }

    fun generatePermanentItemId(): String {
        return UUID.randomUUID().toString()
    }

    fun generatePermanentAuctionId(): String = UUID.randomUUID().toString()

    fun generateOrganizationId(organizationReference: OrganizationReference): String {
        return organizationReference.identifier.scheme + "-" + organizationReference.identifier.id
    }

    fun generateOcid(cpid: String, stage: String): Ocid.SingleStage {
        val cpidParsed = Cpid.tryCreateOrNull(cpid)
            ?: throw ErrorException(ErrorType.INVALID_CPID_FROM_DTO)

        val stageParsed = Stage.orNull(stage)
            ?: throw ErrorException(ErrorType.INVALID_STAGE)

        return Ocid.SingleStage.generate(cpid = cpidParsed, stage = stageParsed, timestamp = nowDefaultUTC())
    }

    fun generateOcid(cpid: Cpid, stage: String): Ocid.SingleStage {
        val stageParsed = Stage.orNull(stage)
            ?: throw ErrorException(ErrorType.INVALID_STAGE)

        return Ocid.SingleStage.generate(cpid = cpid, stage = stageParsed, timestamp = nowDefaultUTC())
    }

    fun criterionId(): String = UUID.randomUUID().toString()

    fun requirementGroupId(): String = UUID.randomUUID().toString()

    fun requirementId(): String = UUID.randomUUID().toString()

    fun conversionId(): String = UUID.randomUUID().toString()

    fun coefficientId(): String = UUID.randomUUID().toString()

    fun lotId(): LotId = UUID.randomUUID()

    fun relatedProcessId(): String = UUID.randomUUID().toString()
}
