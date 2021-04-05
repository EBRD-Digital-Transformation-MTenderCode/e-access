package com.procurement.access.domain.fail.error

import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid

abstract class CommandValidationErrors(
    numberError: String,
    prefix: String = "VR.COM-",
    override val description: String
) : Fail.Error(prefix = prefix) {

    override val code: String = prefix + numberError

    override fun logging(logger: Logger) {
        logger.error(message = message)
    }

    object ValidateRfqData {
        class InvalidNumberOfLots(numberOfLots: Int) : CommandValidationErrors(
            numberError = "46.1.1",
            description = "Expected number of lots: 1. Actual: '$numberOfLots'."
        )

        class PnNotFound(cpid: Cpid, ocid: Ocid) : CommandValidationErrors(
            numberError = "46.1.2",
            description = "Pn record by cpid '$cpid' and ocid '$ocid' not found."
        )

        class InvalidCurrency(expectedCurrency: String, receivedCurrency: String) : CommandValidationErrors(
            numberError = "46.1.3",
            description = "Expected currency: '$expectedCurrency'. Actual: '$receivedCurrency'."
        )

        class InvalidContractPeriod() : CommandValidationErrors(
            numberError = "46.1.4",
            description = "Contract period start date must precede contract period end date."
        )

        class InvalidTenderPeriod() : CommandValidationErrors(
            numberError = "46.1.5",
            description = "Tender period end date must precede contract period start date."
        )

        class DuplicatedItemId(itemId: String) : CommandValidationErrors(
            numberError = "46.1.6",
            description = "Items contain duplicate id '$itemId'."
        )
        
        class UnknownRelatedLot(itemId: String, relatedLot: String) : CommandValidationErrors(
            numberError = "46.1.7",
            description = "Item '$itemId' contains unknown relatedLot '$relatedLot'."
        )

        class ElectronicAuctionsAreMissing() : CommandValidationErrors(
            numberError = "46.1.8",
            description = "Electronic auctions are missing."
        )

        class RedundantElectronicAuctions() : CommandValidationErrors(
            numberError = "46.1.9",
            description = "Request contains redundant electronic auctions."
        )
    }
}
