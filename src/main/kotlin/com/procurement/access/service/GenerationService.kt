package com.procurement.access.service

import com.datastax.driver.core.utils.UUIDs
import com.procurement.access.application.model.Mode
import com.procurement.access.model.dto.ocds.OrganizationReference
import com.procurement.access.utils.milliNowUTC
import org.springframework.stereotype.Service
import java.util.*

@Service
class GenerationService {

    fun getCpId(country: String, mode: Mode): String = mode.prefix + "-" + country + "-" + milliNowUTC()

    fun generatePermanentTenderId(): String {
        return UUID.randomUUID().toString()
    }

    fun generateRandomUUID(): UUID {
        return UUIDs.random()
    }

    fun generatePermanentTenderId(): String {
        return UUID.randomUUID().toString()
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

    fun generateOrganizationId(organizationReference: OrganizationReference): String {
        return organizationReference.identifier.scheme + "-" + organizationReference.identifier.id
    }
}
