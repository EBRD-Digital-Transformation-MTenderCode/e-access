package com.procurement.access.service

import com.procurement.access.application.model.params.ValidateRfqDataParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.Transform
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.CommandValidationErrors
import com.procurement.access.domain.model.enums.ProcurementMethodModalities
import com.procurement.access.infrastructure.configuration.properties.UriProperties
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.lib.extension.getDuplicate
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.lib.functional.asValidationFailure
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service

interface RfqService {
    fun validateRfqData(params: ValidateRfqDataParams): ValidationResult<Fail>
}

@Service
class RfqServiceImpl(
    private val tenderProcessRepository: TenderProcessRepository,
    private val generationService: GenerationService,
    private val uriProperties: UriProperties,
    private val transform: Transform
) : RfqService {
    override fun validateRfqData(params: ValidateRfqDataParams): ValidationResult<Fail> {
        val pnEntity = tenderProcessRepository.getByCpIdAndOcid(params.relatedCpid, params.relatedOcid)
            .onFailure { return it.reason.asValidationFailure() }
            ?: return CommandValidationErrors.ValidateRfqData.PnNotFound(params.relatedCpid, params.relatedOcid)
                .asValidationFailure()


        val pn = pnEntity.jsonData.tryToObject(PNEntity::class.java)
            .onFailure { return it.reason.asValidationFailure() }

        checkLot(params, pn).doOnError { return it.asValidationFailure() }
        checkItems(params).doOnError { return it.asValidationFailure() }
        checkProcurementMethodModalities(params).doOnError { return it.asValidationFailure() }

        return ValidationResult.ok()
    }

    private fun checkLot(
        params: ValidateRfqDataParams,
        pn: PNEntity
    ): ValidationResult<CommandValidationErrors> {
        if (params.tender.lots.size != 1)
            return CommandValidationErrors.ValidateRfqData.InvalidNumberOfLots(params.tender.lots.size)
                .asValidationFailure()

        val lot = params.tender.lots.first()

        checkLotCurrency(lot, pn).doOnError { return it.asValidationFailure() }
        checkLotContractPeriod(lot, params).doOnError { return it.asValidationFailure() }

        return ValidationResult.ok()
    }

    private fun checkLotContractPeriod(
        lot: ValidateRfqDataParams.Tender.Lot,
        params: ValidateRfqDataParams
    ): ValidationResult<CommandValidationErrors> {
        if (!lot.contractPeriod.endDate.isAfter(lot.contractPeriod.startDate))
            return CommandValidationErrors.ValidateRfqData.InvalidContractPeriod().asValidationFailure()

        if (!lot.contractPeriod.startDate.isAfter(params.tender.tenderPeriod.endDate))
            return CommandValidationErrors.ValidateRfqData.InvalidTenderPeriod().asValidationFailure()

        return ValidationResult.ok()
    }

    private fun checkLotCurrency(
        lot: ValidateRfqDataParams.Tender.Lot,
        pn: PNEntity
    ): ValidationResult<CommandValidationErrors.ValidateRfqData.InvalidCurrency> {
        val requestCurrency = lot.value.currency
        val storedCurrency = pn.tender.value.currency

        if (requestCurrency != storedCurrency)
            return CommandValidationErrors.ValidateRfqData.InvalidCurrency(
                expectedCurrency = storedCurrency, receivedCurrency = requestCurrency
            ).asValidationFailure()

        return ValidationResult.ok()
    }

    private fun checkItems(params: ValidateRfqDataParams): ValidationResult<CommandValidationErrors> {
        val lot = params.tender.lots.first()

        val duplicateItemId = params.tender.items.getDuplicate { it.id }
        if (duplicateItemId != null)
            return CommandValidationErrors.ValidateRfqData.DuplicatedItemId(duplicateItemId.id).asValidationFailure()

        val itemWithUnknownLot = params.tender.items.firstOrNull { it.relatedLot != lot.id }
        if (itemWithUnknownLot != null)
            return CommandValidationErrors.ValidateRfqData.UnknownRelatedLot(
                itemId = itemWithUnknownLot.id, relatedLot = itemWithUnknownLot.relatedLot
            ).asValidationFailure()

        return ValidationResult.ok()
    }

    private fun checkProcurementMethodModalities(params: ValidateRfqDataParams): ValidationResult<CommandValidationErrors> {
        val procurementMethodModalities = params.tender.procurementMethodModalities
        val electronicAuctions = params.tender.electronicAuctions

        when (procurementMethodModalities.contains(ProcurementMethodModalities.ELECTRONIC_AUCTION)) {
            true -> if (electronicAuctions == null)
                return CommandValidationErrors.ValidateRfqData.ElectronicAuctionsAreMissing().asValidationFailure()
            false -> if (electronicAuctions != null)
                return CommandValidationErrors.ValidateRfqData.RedundantElectronicAuctions().asValidationFailure()
        }

        return ValidationResult.ok()
    }
}
