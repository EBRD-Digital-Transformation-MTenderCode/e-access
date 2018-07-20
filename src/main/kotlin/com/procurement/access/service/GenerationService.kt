package com.procurement.access.service

import com.datastax.driver.core.utils.UUIDs
import com.procurement.access.config.OCDSProperties
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.dto.ocds.OrganizationReference
import com.procurement.access.utils.milliNowUTC
import org.springframework.stereotype.Service
import java.util.*

interface GenerationService {

    fun getCpId(country: String): String

    fun generateRandomUUID(): UUID

    fun generateTimeBasedUUID(): UUID

    fun generateOrganizationId(organizationReference: OrganizationReference): String
}

@Service
class GenerationServiceImpl(private val ocdsProperties: OCDSProperties) : GenerationService {

    override fun getCpId(country: String): String {
        return ocdsProperties.prefix + "-" + country + "-" + milliNowUTC()
    }

    override fun generateRandomUUID(): UUID {
        return UUIDs.random()
    }

    override fun generateTimeBasedUUID(): UUID {
        return UUIDs.timeBased()
    }

    override fun generateOrganizationId(organizationReference: OrganizationReference): String {
        return organizationReference.identifier.scheme + "-" + organizationReference.identifier.id
    }

}