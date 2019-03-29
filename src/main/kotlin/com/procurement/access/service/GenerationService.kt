package com.procurement.access.service

import com.datastax.driver.core.utils.UUIDs
import com.procurement.access.config.OCDSProperties
import com.procurement.access.model.dto.ocds.OrganizationReference
import com.procurement.access.utils.milliNowUTC
import org.springframework.stereotype.Service
import java.util.*

@Service
class GenerationService(private val ocdsProperties: OCDSProperties) {

    fun getCpId(country: String): String {
        return ocdsProperties.prefix + "-" + country + "-" + milliNowUTC()
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
