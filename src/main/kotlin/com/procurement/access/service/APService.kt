package com.procurement.access.service

import com.procurement.access.application.model.params.AddClientsToPartiesInAPParams
import com.procurement.access.application.model.params.CalculateAPValueParams
import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.ap.get.GetAPTitleAndDescriptionContext
import com.procurement.access.application.service.ap.get.GetAPTitleAndDescriptionResult
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.enums.MainGeneralActivity
import com.procurement.access.domain.model.enums.MainSectoralActivity
import com.procurement.access.domain.model.enums.PartyRole
import com.procurement.access.domain.model.enums.RelatedProcessType
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.infrastructure.entity.process.RelatedProcess
import com.procurement.access.infrastructure.handler.v2.model.response.AddClientsToPartiesInAPResult
import com.procurement.access.infrastructure.handler.v2.model.response.CalculateAPValueResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.Result.Companion.failure
import com.procurement.access.lib.functional.Result.Companion.success
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toObject
import com.procurement.access.utils.trySerialization
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service
import java.math.BigDecimal

interface APService {
    fun calculateAPValue(params: CalculateAPValueParams): Result<CalculateAPValueResult, Fail>
    fun getAPTitleAndDescription(context: GetAPTitleAndDescriptionContext): GetAPTitleAndDescriptionResult
    fun addClientsToPartiesInAP(params: AddClientsToPartiesInAPParams): Result<AddClientsToPartiesInAPResult, Fail>
}

@Service
class APServiceImpl(
    private val tenderProcessRepository: TenderProcessRepository,
    private val logger: Logger
) : APService {

    override fun calculateAPValue(params: CalculateAPValueParams): Result<CalculateAPValueResult, Fail> {

        // FR.COM-1.31.1
        val entity = tenderProcessRepository.getByCpIdAndStage(params.cpid, params.ocid.stage)
            .onFailure { fail -> return fail }
            ?: return failure( // VR.COM-1.31.1
                ValidationErrors.TenderNotFoundOnCalculateAPValue(params.cpid, params.ocid)
            )

        val ap = entity.jsonData.tryToObject(APEntity::class.java)
            .onFailure { fail -> return fail }

        // FR.COM-1.31.2
        val relatedPNProcesses = ap.relatedProcesses.orEmpty()
            .filter(::isRelatedToPN)

        // VR.COM-1.31.2
        if (relatedPNProcesses.isEmpty())
            return failure(ValidationErrors.RelationNotFoundOnCalculateAPValue(params.cpid, params.ocid))

        val relatedPns = relatedPNProcesses.map { pnProcess ->
            parseCpid(pnProcess.identifier.value)
                .flatMap { parsedCpid -> tenderProcessRepository.getByCpIdAndStage(parsedCpid, Stage.PN) }
                .flatMap { pnEntity -> pnEntity!!.jsonData.tryToObject(PNEntity::class.java) }
                .onFailure { fail -> return fail }
        }

        // FR.COM-1.31.3
        // FR.COM-1.31.4
        val relatedPnsValueSum = relatedPns
            .map { it.tender.value.amount }
            .fold(BigDecimal.ZERO, BigDecimal::add)

        val apTenderValue = ap.tender.value.copy(amount = relatedPnsValueSum)

        val updatedAp = ap.copy(
            tender = ap.tender.copy(
                value = apTenderValue
            )
        )

        val updatedJsonData = trySerialization(updatedAp)
            .onFailure { fail -> return fail }

        val updatedEntity = entity.copy(jsonData = updatedJsonData)

        // FR.COM-1.31.6
        tenderProcessRepository.update(entity = updatedEntity)

        val result = CalculateAPValueResult(
            CalculateAPValueResult.Tender(
                value = CalculateAPValueResult.Tender.Value(
                    amount = apTenderValue.amount!!,
                    currency = apTenderValue.currency
                )
            )
        )

        return success(result)
    }

    override fun getAPTitleAndDescription(context: GetAPTitleAndDescriptionContext): GetAPTitleAndDescriptionResult {
        val cpid = Cpid.tryCreate(context.cpid)
            .orThrow { pattern ->
                ErrorException(
                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                    message = "cpid '${context.cpid}' mismatch to pattern $pattern."
                )
            }

        /**
         * Why stage getting not from context?,
         * Because this command now used in PCR process, so there is no place when we can get appropriate stage
         */
        val stage = Stage.AP

        val entity = tenderProcessRepository.getByCpIdAndStage(cpid = cpid, stage = stage)
            .orThrow { fail -> throw fail.exception }
            ?: throw ErrorException(
                error = ErrorType.ENTITY_NOT_FOUND,
                message = "VR.COM-1.35.1. Cannot found record by cpid = '$cpid' and stage = '$stage' "
            )

        val ap = toObject(APEntity::class.java, entity.jsonData)

        return GetAPTitleAndDescriptionResult(title = ap.tender.title, description = ap.tender.description)
    }

    private fun isRelatedToPN(relatedProcess: RelatedProcess): Boolean =
        relatedProcess.relationship.any { relationship -> relationship == RelatedProcessType.X_SCOPE }

    override fun addClientsToPartiesInAP(params: AddClientsToPartiesInAPParams): Result<AddClientsToPartiesInAPResult, Fail> {
        val pnEntity = tenderProcessRepository.getByCpIdAndStage(params.relatedCpid, params.relatedOcid.stage)
            .onFailure { fail -> return fail }
            ?: return failure(
                ValidationErrors.AddClientsToPartiesInAP.PnRecordNotFound(params.relatedCpid, params.relatedOcid)
            )

        val pn = pnEntity.jsonData.tryToObject(PNEntity::class.java)
            .onFailure { fail -> return fail }

        if (pn.buyer == null)
            return failure(ValidationErrors.AddClientsToPartiesInAP.BuyerIsMissing())

        val apEntity = tenderProcessRepository.getByCpIdAndStage(params.cpid, params.ocid.stage)
            .onFailure { fail -> return fail }
            ?: return failure(ValidationErrors.AddClientsToPartiesInAP.ApRecordNotFound(params.cpid, params.ocid))

        val ap = apEntity.jsonData.tryToObject(APEntity::class.java)
            .onFailure { fail -> return fail }

        val clientParty = ap.parties
            .firstOrNull { party -> containsPnBuyerOfClientRole(party, pn.buyer) }
            ?: createAndSaveClientParty(pn.buyer, ap, apEntity)
                .onFailure { fail -> return fail }

        return generateResult(clientParty).asSuccess()
    }

    private fun containsPnBuyerOfClientRole(
        party: APEntity.Party,
        buyer: PNEntity.Buyer
    ) = (party.id == buyer.id
        && party.roles.contains(PartyRole.CLIENT))

    private fun createAndSaveClientParty(
        buyer: PNEntity.Buyer,
        ap: APEntity,
        apEntity: TenderProcessEntity
    ): Result<APEntity.Party, Fail> {
        val createdClientParty = buyer.toParty(listOf(PartyRole.CLIENT))
        val updatedParties = ap.parties + createdClientParty
        val updatedAp = ap.copy(parties = updatedParties)
        val updatedJsonData = trySerialization(updatedAp)
            .onFailure { fail -> return fail }
        val updatedApEntity = apEntity.copy(jsonData = updatedJsonData)
        tenderProcessRepository.save(updatedApEntity)

        return createdClientParty.asSuccess()
    }

    private fun PNEntity.Buyer.toParty(roles: List<PartyRole>): APEntity.Party =
        APEntity.Party(
            id = id,
            name = name,
            identifier = identifier
                .let { identifier ->
                    APEntity.Party.Identifier(
                        scheme = identifier.scheme,
                        id = identifier.id,
                        legalName = identifier.legalName,
                        uri = identifier.uri
                    )
                },
            additionalIdentifiers = additionalIdentifiers
                ?.map { additionalIdentifier ->
                    APEntity.Party.AdditionalIdentifier(
                        scheme = additionalIdentifier.scheme,
                        id = additionalIdentifier.id,
                        legalName = additionalIdentifier.legalName,
                        uri = additionalIdentifier.uri
                    )
                },
            address = address
                .let { address ->
                    APEntity.Party.Address(
                        streetAddress = address.streetAddress,
                        postalCode = address.postalCode,
                        addressDetails = address.addressDetails
                            .let { addressDetails ->
                                APEntity.Party.Address.AddressDetails(
                                    country = addressDetails.country
                                        .let { country ->
                                            APEntity.Party.Address.AddressDetails.Country(
                                                scheme = country.scheme,
                                                id = country.id,
                                                description = country.description,
                                                uri = country.uri
                                            )
                                        },
                                    region = addressDetails.region
                                        .let { region ->
                                            APEntity.Party.Address.AddressDetails.Region(
                                                scheme = region.scheme,
                                                id = region.id,
                                                description = region.description,
                                                uri = region.uri
                                            )
                                        },
                                    locality = addressDetails.locality
                                        .let { locality ->
                                            APEntity.Party.Address.AddressDetails.Locality(
                                                scheme = locality.scheme,
                                                id = locality.id,
                                                description = locality.description,
                                                uri = locality.uri
                                            )
                                        }
                                )
                            }
                    )
                },
            contactPoint = contactPoint
                .let { contactPoint ->
                    APEntity.Party.ContactPoint(
                        name = contactPoint.name,
                        email = contactPoint.email,
                        telephone = contactPoint.telephone,
                        faxNumber = contactPoint.faxNumber,
                        url = contactPoint.url
                    )
                },
            details = APEntity.Party.Details(
                mainSectoralActivity = details?.mainSectoralActivity?.let { MainSectoralActivity.creator(it) },
                mainGeneralActivity = details?.mainGeneralActivity?.let { MainGeneralActivity.creator(it) },
                typeOfBuyer = details?.typeOfBuyer
            ),
            roles = roles
        )

    private fun generateResult(party: APEntity.Party) =
        AddClientsToPartiesInAPResult(
            parties = AddClientsToPartiesInAPResult.Party(
                id = party.id,
                name = party.name,
                identifier = party.identifier
                    .let { identifier ->
                        AddClientsToPartiesInAPResult.Party.Identifier(
                            scheme = identifier.scheme,
                            id = identifier.id,
                            legalName = identifier.legalName,
                            uri = identifier.uri
                        )
                    },
                additionalIdentifiers = party.additionalIdentifiers
                    ?.map { additionalIdentifier ->
                        AddClientsToPartiesInAPResult.Party.AdditionalIdentifier(
                            scheme = additionalIdentifier.scheme,
                            id = additionalIdentifier.id,
                            legalName = additionalIdentifier.legalName,
                            uri = additionalIdentifier.uri
                        )
                    },
                address = party.address
                    .let { address ->
                        AddClientsToPartiesInAPResult.Party.Address(
                            streetAddress = address.streetAddress,
                            postalCode = address.postalCode,
                            addressDetails = address.addressDetails
                                .let { addressDetails ->
                                    AddClientsToPartiesInAPResult.Party.Address.AddressDetails(
                                        country = addressDetails.country
                                            .let { country ->
                                                AddClientsToPartiesInAPResult.Party.Address.AddressDetails.Country(
                                                    scheme = country.scheme,
                                                    id = country.id,
                                                    description = country.description,
                                                    uri = country.uri
                                                )
                                            },
                                        region = addressDetails.region
                                            .let { region ->
                                                AddClientsToPartiesInAPResult.Party.Address.AddressDetails.Region(
                                                    scheme = region.scheme,
                                                    id = region.id,
                                                    description = region.description,
                                                    uri = region.uri
                                                )
                                            },
                                        locality = addressDetails.locality
                                            .let { locality ->
                                                AddClientsToPartiesInAPResult.Party.Address.AddressDetails.Locality(
                                                    scheme = locality.scheme,
                                                    id = locality.id,
                                                    description = locality.description,
                                                    uri = locality.uri
                                                )
                                            }
                                    )
                                }
                        )
                    },
                contactPoint = party.contactPoint
                    .let { contactPoint ->
                        AddClientsToPartiesInAPResult.Party.ContactPoint(
                            name = contactPoint.name,
                            email = contactPoint.email,
                            telephone = contactPoint.telephone,
                            faxNumber = contactPoint.faxNumber,
                            url = contactPoint.url
                        )
                    },
                details = party.details.let { details ->
                    AddClientsToPartiesInAPResult.Party.Details(
                        mainSectoralActivity = details?.mainSectoralActivity,
                        mainGeneralActivity = details?.mainGeneralActivity,
                        typeOfBuyer = details?.typeOfBuyer
                    )
                },
                roles = party.roles
            ).let { listOf(it) }
        )
}
