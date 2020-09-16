package com.procurement.access.service

import com.procurement.access.application.service.ap.update.ApUpdateData
import com.procurement.access.application.service.ap.update.UpdateApContext
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.money.Money
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType.DATA_NOT_FOUND
import com.procurement.access.exception.ErrorType.EMPTY_DOCS
import com.procurement.access.exception.ErrorType.INCORRECT_VALUE_ATTRIBUTE
import com.procurement.access.exception.ErrorType.INVALID_DOCS_ID
import com.procurement.access.exception.ErrorType.INVALID_DOCS_RELATED_LOTS
import com.procurement.access.exception.ErrorType.INVALID_ITEMS_QUANTITY
import com.procurement.access.exception.ErrorType.INVALID_ITEMS_RELATED_LOTS
import com.procurement.access.exception.ErrorType.INVALID_LOT_CONTRACT_PERIOD
import com.procurement.access.exception.ErrorType.INVALID_LOT_CURRENCY
import com.procurement.access.exception.ErrorType.INVALID_OWNER
import com.procurement.access.exception.ErrorType.INVALID_START_DATE
import com.procurement.access.exception.ErrorType.INVALID_TOKEN
import com.procurement.access.exception.ErrorType.NOT_UNIQUE_IDS
import com.procurement.access.infrastructure.dto.ap.update.ApUpdateResponse
import com.procurement.access.infrastructure.dto.ap.update.converter.convert
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.lib.toSetBy
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

interface ApUpdateService {
    fun updateAp(context: UpdateApContext, data: ApUpdateData): ApUpdateResponse
}

@Service
class ApUpdateServiceImpl(
    private val generationService: GenerationService,
    private val tenderProcessDao: TenderProcessDao
): ApUpdateService {

    override fun updateAp(context: UpdateApContext, data: ApUpdateData): ApUpdateResponse {

        val entity = tenderProcessDao.getByCpIdAndStage(context.cpid, context.stage)
            ?: throw ErrorException(DATA_NOT_FOUND)

        // VR.COM-1.26.1
        if (entity.token != context.token) throw ErrorException(INVALID_TOKEN)
        // VR.COM-1.26.2
        if (entity.owner != context.owner) throw ErrorException(INVALID_OWNER)

        val tenderProcess = toObject(APEntity::class.java, entity.jsonData)

        // VR.COM-1.26.6
        validateStartDate(data.tender.tenderPeriod.startDate)

        val activeLots = data.tender.lots

        // FR.COM-1.26.8
        val updatedContractPeriod =
            if (activeLots.isNotEmpty())
                calculateContractPeriod(activeLots)
            else
                tenderProcess.tender.contractPeriod

        // FR.COM-1.26.9
        val updatedValue =
            if (activeLots.isNotEmpty()) {
                val newAmount = calculateValueByActiveLots(activeLots)
                if (tenderProcess.tender.value != null)
                    Money(newAmount, tenderProcess.tender.value.currency)
                else {
                    checkLotsCurrency(activeLots)
                    Money(newAmount, activeLots.first().value.currency)
                }
            } else
                tenderProcess.tender.value

        // FR.COM-1.26.3
        val updatedTitle = data.tender.title

        // FR.COM-1.26.3
        val updatedDescription = data.tender.description

        // FR.COM-1.26.3
        val updatedProcurementMethodRationale = data.tender.procurementMethodRationale ?: tenderProcess.tender.procurementMethodRationale

        // FR.COM-1.26.3
        val updatedClassification = data.tender.classification
            ?.let { receivedClassification ->
                APEntity.Tender.Classification(
                    id = receivedClassification.id,
                    scheme = receivedClassification.scheme,
                    description = receivedClassification.description
                )
            }
            ?: tenderProcess.tender.classification

        // FR.COM-1.26.3
        val updatedTenderPeriod = APEntity.Tender.TenderPeriod(startDate = data.tender.tenderPeriod.startDate)

        // FR.COM-1.26.3
        val updatedMainProcurementCategory = data.tender.mainProcurementCategory ?: tenderProcess.tender.mainProcurementCategory

        val temporalToPermanentLotId = mutableMapOf<String, String>()
        val updatedLots =
            if (data.tender.lots.isNotEmpty()) {

                // VR.COM-1.26.4
                checkLotsIdsQniqueness(data.tender.lots.map { it.id })

                // VR.COM-1.26.10
                checkLotsContractPeriod(data.tender.lots, updatedTenderPeriod.startDate)

                // VR.COM-1.26.14
                checkPlaceOfPerformance(activeLots, data.tender.items)

                val receivedLotsById = data.tender.lots.associateBy { it.id }
                val updatedLots = tenderProcess.tender.lots.orEmpty()
                    .map { lotFromDb ->
                        receivedLotsById[lotFromDb.id]
                            ?.let { lotFromDb.updateBy(it) }
                            ?: lotFromDb.copy(
                                status = LotStatus.CANCELLED,
                                statusDetails = LotStatusDetails.EMPTY
                            )
                    }

                val receivedLotsIds = receivedLotsById.keys
                val availableLotsIds = tenderProcess.tender.items.orEmpty().toSetBy { it.id }
                val newLotsId = receivedLotsIds - availableLotsIds
                val newLots = newLotsId.map { newLotId ->
                    receivedLotsById.getValue(newLotId).createEntity()
                        .also { createLot -> temporalToPermanentLotId.put(newLotId, createLot.id) }
                }
                updatedLots + newLots
            } else {
                tenderProcess.tender.lots
            }

        val updatedItems =
            if (data.tender.items.isNotEmpty()) {
                // VR.COM-1.26.4
                checkItemsIdsQniqueness(data.tender.items.map { it.id })

                // VR.COM-1.26.11
                checkQuantity(data.tender.items.map { it.quantity })

                // VR.COM-1.26.12
                checkItemsRelation(data.tender.items, data.tender.lots.map { it.id })

                val receivedItemsById = data.tender.items.associateBy { it.id }
                val updatedItems = tenderProcess.tender.items.orEmpty()
                    .map { itemFromDb ->
                        receivedItemsById[itemFromDb.id]
                            ?.let { itemFromDb.updateBy(it) }
                            ?: itemFromDb.copy(quantity = BigDecimal.ZERO)
                    }

                val receivedItemsIds = receivedItemsById.keys
                val availableItemsIds = tenderProcess.tender.items.orEmpty().toSetBy { it.id }
                val newItemsId = receivedItemsIds - availableItemsIds
                val newItems = newItemsId.map { newItemId ->
                    receivedItemsById.getValue(newItemId).createEntity(temporalToPermanentLotId)
                }
                updatedItems + newItems
            } else {
                tenderProcess.tender.items
            }

        // FR.COM-1.26.4
        val updatedTenderDocuments = updateTenderDocuments(
            tender = tenderProcess.tender,
            receivedDocuments = data.tender.documents,
            temporalToPermanentLotId = temporalToPermanentLotId
        )

        val updatedTenderProcess = tenderProcess.copy(
            tender = tenderProcess.tender.copy(
                contractPeriod = updatedContractPeriod,
                value = updatedValue,
                title = updatedTitle,
                description = updatedDescription,
                procurementMethodRationale = updatedProcurementMethodRationale,
                classification = updatedClassification,
                tenderPeriod = updatedTenderPeriod,
                mainProcurementCategory = updatedMainProcurementCategory,
                lots = updatedLots,
                items = updatedItems,
                documents = updatedTenderDocuments
            )
        )

        tenderProcessDao.save(getEntity(updatedTenderProcess, entity, context.startDate))
        return updatedTenderProcess.convert()
    }

    private fun calculateContractPeriod(activeLots: List<ApUpdateData.Tender.Lot>): APEntity.Tender.ContractPeriod {
        val contractPeriodSet = activeLots.asSequence()
            .map { it.contractPeriod }.toSet()

        return contractPeriodSet
            .let { contractPeriods ->
                val startDate = contractPeriods.minBy { it.startDate }!!.startDate
                val endDate = contractPeriods.maxBy { it.endDate }!!.endDate
                APEntity.Tender.ContractPeriod(startDate, endDate)
            }
    }

    private fun calculateValueByActiveLots(activeLots: List<ApUpdateData.Tender.Lot>): BigDecimal =
        activeLots.asSequence()
            .sumByDouble { it.value.amount.toDouble() }
            .toBigDecimal().setScale(2, RoundingMode.HALF_UP)

    private fun checkItemsIdsQniqueness(itemsIds: List<String>) {
        val uniqItemsIds = itemsIds.toSetBy { it }
        if (uniqItemsIds.size != itemsIds.size)
            throw ErrorException(
                error = NOT_UNIQUE_IDS,
                message = "Not unique item ids."
            )
    }

    private fun checkLotsIdsQniqueness(lotsIds: List<String>) {
        val uniqLotsIds = lotsIds.toSetBy { it }
        if (uniqLotsIds.size != lotsIds.size)
            throw ErrorException(
                error = NOT_UNIQUE_IDS,
                message = "Not unique lots ids."
            )
    }

    private fun checkPlaceOfPerformance(lots: List<ApUpdateData.Tender.Lot>, items: List<ApUpdateData.Tender.Item>) {
        val itemsByRelatedLot = items.groupBy { it.relatedLot }

        lots.forEach { lot ->
            if (lot.placeOfPerformance == null) {
                val relatedItems = itemsByRelatedLot.getOrDefault(lot.id, emptyList())
                if (relatedItems.isEmpty())
                    throw ErrorException(
                        error = INCORRECT_VALUE_ATTRIBUTE,
                        message = "Missing 'placeOfPerformance' attribute."
                    )
                else
                    relatedItems.forEach { relatedItem ->
                        if (relatedItem.deliveryAddress != null)
                            throw ErrorException(
                                error = INCORRECT_VALUE_ATTRIBUTE,
                                message = "Missing 'deliveryAddress' in item='${relatedItem.id}'."
                            )
                    }
            }
        }
    }

    fun checkQuantity(quantities: List<BigDecimal>) {
        quantities.forEach { quantity ->
            if (quantity <= BigDecimal.ZERO)
                throw ErrorException(
                    error = INVALID_ITEMS_QUANTITY,
                    message = "Quantity must be greater than 0."
                )
        }
    }

    fun checkItemsRelation(items: List<ApUpdateData.Tender.Item>, receivedLotsIds: List<String>) {
        items.map { it.relatedLot }
            .forEach { relatedLot ->
                if (relatedLot !in receivedLotsIds)
                    throw ErrorException(
                        error = INVALID_ITEMS_RELATED_LOTS,
                        message = "Cannot find lot with id '${relatedLot}' in request."
                    )
            }
    }

    private fun validateStartDate(startDate: LocalDateTime) {
        if (startDate.dayOfMonth != 1) throw ErrorException(INVALID_START_DATE)
    }

    private fun checkLotsCurrency(lots: List<ApUpdateData.Tender.Lot>) {
        val currencies = lots.toSetBy { it.value.currency }
        if (currencies.size != 1)
            throw ErrorException(
                error = INVALID_LOT_CURRENCY,
                message = "Found ${currencies.size} currencies: ${currencies}"
            )
    }

    private fun checkLotsContractPeriod(lots: List<ApUpdateData.Tender.Lot>, tenderPeriodStartDate: LocalDateTime) {
        lots.map { it.contractPeriod }
            .forEach {
                if (it.startDate >= it.endDate) throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
                if (it.startDate < tenderPeriodStartDate) throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
            }
    }

    private fun updateTenderDocuments(
        tender: APEntity.Tender,
        receivedDocuments: List<ApUpdateData.Tender.Document>,
        temporalToPermanentLotId: Map<String, String>
    ): List<APEntity.Tender.Document> {
        if (receivedDocuments.isNotEmpty()) {
            val uniqDocsId = receivedDocuments.toSetBy { it.id }
            if (uniqDocsId.size != receivedDocuments.size) throw ErrorException(INVALID_DOCS_ID)

            // VR.COM-1.26.7
            val receivedLotsIds = tender.lots.orEmpty()
                .toSetBy { it.id }
            validateDocumentsRelatedLots(receivedDocuments, receivedLotsIds)

            return if (tender.documents.orEmpty().isNotEmpty()) {
                val documentsDb = tender.documents.orEmpty()

                // VR.COM-1.26.3
                val receivedDocumentsIds = receivedDocuments.toSetBy { it.id }
                val availableDocumentsIds = documentsDb.toSetBy { it.id }
                if (!receivedDocumentsIds.containsAll(availableDocumentsIds))
                    throw ErrorException(
                        error = INVALID_DOCS_ID,
                        message = "Missing documents ( ids: ${availableDocumentsIds - receivedDocumentsIds}) in request."
                    )

                val receivedDocumentsById = receivedDocuments.associateBy { it.id }

                val updatedDocuments = documentsDb.map { documentFromDb ->
                    receivedDocumentsById[documentFromDb.id]
                        ?.let { documentFromDb.updateBy(it) }
                        ?: documentFromDb
                }
                val newDocumentsId = receivedDocumentsIds - availableDocumentsIds
                val newDocuments = newDocumentsId.map { newDocumentId ->
                    receivedDocumentsById.getValue(newDocumentId).createEntity(temporalToPermanentLotId)
                }
                updatedDocuments + newDocuments
            } else {
                receivedDocuments.map { it.createEntity(temporalToPermanentLotId) }
            }
        } else {
            if (tender.documents.orEmpty().isNotEmpty())
                throw ErrorException(
                    error = EMPTY_DOCS,
                    message = "Missing documents in request."
                )
        }

        return emptyList()
    }

    private fun updateAdditionalClassifications(
        received: List<ApUpdateData.Tender.Item.AdditionalClassification>,
        stored: List<APEntity.Tender.Item.AdditionalClassification>
    ): List<APEntity.Tender.Item.AdditionalClassification> =
        if (received.isNotEmpty()) {
            val receivedAcById = received.associateBy { it.id }
            val updatedAc = stored
                .map { acFromDb ->
                    receivedAcById[acFromDb.id]
                        ?.let { acFromDb.updateBy(it) }
                        ?: acFromDb
                }

            val receivedAcIds = receivedAcById.keys
            val availableAcIds = stored.toSetBy { it.id }
            val newAcsId = receivedAcIds - availableAcIds
            val newAc = newAcsId.map { newAcId ->
                receivedAcById.getValue(newAcId).toDomain()
            }
            updatedAc + newAc
        } else {
            stored
        }

    private fun APEntity.Tender.Item.AdditionalClassification.updateBy(received: ApUpdateData.Tender.Item.AdditionalClassification) =
        APEntity.Tender.Item.AdditionalClassification(
            id = received.id,
            scheme = received.scheme,
            description = received.description
        )

    private fun ApUpdateData.Tender.Item.AdditionalClassification.toDomain() =
        APEntity.Tender.Item.AdditionalClassification(
            id = this.id,
            scheme = this.scheme,
            description = this.description
        )

    // VR.COM-1.26.7
    // VR.COM-1.26.13
    private fun validateDocumentsRelatedLots(
        documents: List<ApUpdateData.Tender.Document>,
        lotsIds: Collection<String>
    ) {
        val lotsFromReceivedDocuments = documents
            .flatMap { it.relatedLots }
            .toSet()

        if (lotsFromReceivedDocuments.isNotEmpty()) {
            if (!lotsIds.containsAll(lotsFromReceivedDocuments))
                throw ErrorException(INVALID_DOCS_RELATED_LOTS)
        }
    }

    private fun APEntity.Tender.Document.updateBy(received: ApUpdateData.Tender.Document): APEntity.Tender.Document =
        this.copy(
            title = received.title,
            description = received.description ?: this.description,
            relatedLots = received.relatedLots.union(this.relatedLots.orEmpty()).toList()
        )

    private fun APEntity.Tender.Item.updateBy(received: ApUpdateData.Tender.Item): APEntity.Tender.Item =
        this.copy(
            description = received.description,
            internalId = received.internalId ?: this.internalId,
            relatedLot = received.relatedLot,
            deliveryAddress = received.deliveryAddress
                ?.let {
                    this.deliveryAddress
                        ?.updateBy(it)
                        ?: it.toEntity()
                }
                ?: this.deliveryAddress
            ,
            classification = this.classification.updateBy(received.classification),
            additionalClassifications = updateAdditionalClassifications(
                received.additionalClassifications,
                this.additionalClassifications.orEmpty()
            ),
            quantity = received.quantity,
            unit = this.unit.updateBy(received.unit)
        )

    private fun APEntity.Tender.Lot.updateBy(received: ApUpdateData.Tender.Lot): APEntity.Tender.Lot =
        this.copy(
            description = received.description,
            title = received.title,
            internalId = received.internalId ?: this.internalId,
            status = received.status,
            statusDetails = received.statusDetails,
            value = received.value,
            contractPeriod = this.contractPeriod.updateBy(received.contractPeriod)
        )

    private fun ApUpdateData.Tender.Lot.createEntity(): APEntity.Tender.Lot =
        APEntity.Tender.Lot(
            id = generationService.generatePermanentLotId(),
            description = this.description,
            title = this.title,
            internalId = this.internalId ?: this.internalId,
            status = LotStatus.PLANNING,
            statusDetails = LotStatusDetails.EMPTY,
            value = this.value,
            contractPeriod = this.contractPeriod.toEntity(),
            placeOfPerformance = this.placeOfPerformance?.toEntity()
        )

    private fun ApUpdateData.Tender.Document.createEntity(temporalToPermanentLotId: Map<String, String>): APEntity.Tender.Document =
        APEntity.Tender.Document(
            id = this.id,
            description = this.description,
            title = this.title,
            documentType = this.documentType,
            relatedLots = this.relatedLots.map { temporalToPermanentLotId.getValue(it) }
        )

    private fun ApUpdateData.Tender.Lot.ContractPeriod.toEntity(): APEntity.Tender.Lot.ContractPeriod =
        APEntity.Tender.Lot.ContractPeriod(
            startDate = this.startDate,
            endDate = this.endDate
        )

    private fun ApUpdateData.Tender.Lot.PlaceOfPerformance.toEntity(): APEntity.Tender.Lot.PlaceOfPerformance =
        APEntity.Tender.Lot.PlaceOfPerformance(
            address = this.address.toEntity()
        )

    private fun APEntity.Tender.Lot.ContractPeriod.updateBy(received: ApUpdateData.Tender.Lot.ContractPeriod): APEntity.Tender.Lot.ContractPeriod =
        this.copy(
            startDate = received.startDate,
            endDate = received.endDate
        )

    private fun APEntity.Tender.Item.Unit.updateBy(received: ApUpdateData.Tender.Item.Unit): APEntity.Tender.Item.Unit =
        this.copy(
            id = received.id,
            name = received.name
        )

    private fun APEntity.Tender.Item.Classification.updateBy(received: ApUpdateData.Tender.Item.Classification): APEntity.Tender.Item.Classification =
        this.copy(
            id = received.id,
            scheme = received.scheme,
            description = received.description
        )

    private fun APEntity.Tender.Address.updateBy(received: ApUpdateData.Tender.Address): APEntity.Tender.Address =
        this.copy(
            streetAddress = received.streetAddress,
            postalCode = received.postalCode ?: this.postalCode,
            addressDetails = this.addressDetails.updateBy(received.addressDetails)
        )

    private fun APEntity.Tender.Address.AddressDetails.updateBy(received: ApUpdateData.Tender.Address.AddressDetails): APEntity.Tender.Address.AddressDetails =
        this.copy(
            country = received.country
                .let { country ->
                    this.country.copy(
                        scheme = country.scheme,
                        id = country.id,
                        description = country.description,
                        uri = country.uri
                    )
                },
            region = received.region
                .let { region ->
                    this.region.copy(
                        scheme = region.scheme,
                        id = region.id,
                        description = region.description,
                        uri = region.uri
                    )
                },
            locality = received.locality
                ?.let { locality ->
                    this.locality
                        ?.copy(
                            scheme = locality.scheme,
                            id = locality.id,
                            description = locality.description,
                            uri = locality.uri ?: this.locality.uri
                        )
                        ?: APEntity.Tender.Address.AddressDetails.Locality(
                            scheme = locality.scheme,
                            id = locality.id,
                            description = locality.description,
                            uri = locality.uri
                        )
                }
                ?: this.locality
        )

    private fun ApUpdateData.Tender.Item.createEntity(temporalToPermanentLotId: Map<String, String>): APEntity.Tender.Item =
        APEntity.Tender.Item(
            id = generationService.generatePermanentItemId(),
            internalId = this.internalId,
            description = this.description,
            classification = this.classification.let { classification ->
                APEntity.Tender.Item.Classification(
                    scheme = classification.scheme,
                    id = classification.id,
                    description = classification.description
                )
            },
            additionalClassifications = this.additionalClassifications
                .map { additionalClassification ->
                    APEntity.Tender.Item.AdditionalClassification(
                        scheme = additionalClassification.scheme,
                        id = additionalClassification.id,
                        description = additionalClassification.description
                    )
                },
            quantity = this.quantity,
            unit = this.unit
                .let { unit ->
                    APEntity.Tender.Item.Unit(
                        id = unit.id,
                        name = unit.name
                    )
                },
            deliveryAddress = this.deliveryAddress
                ?.let { address ->
                    APEntity.Tender.Address(
                        streetAddress = address.streetAddress,
                        postalCode = address.postalCode,
                        addressDetails = address.addressDetails
                            .let { addressDetails ->
                                APEntity.Tender.Address.AddressDetails(
                                    country = addressDetails.country
                                        .let { country ->
                                            APEntity.Tender.Address.AddressDetails.Country(
                                                scheme = country.scheme,
                                                id = country.id,
                                                description = country.description,
                                                uri = country.uri
                                            )
                                        },
                                    region = addressDetails.region
                                        .let { region ->
                                            APEntity.Tender.Address.AddressDetails.Region(
                                                scheme = region.scheme,
                                                id = region.id,
                                                description = region.description,
                                                uri = region.uri
                                            )
                                        },
                                    locality = addressDetails.locality
                                        ?.let { locality ->
                                            APEntity.Tender.Address.AddressDetails.Locality(
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
            relatedLot = temporalToPermanentLotId.getValue(this.relatedLot)
        )

    private fun ApUpdateData.Tender.Address.toEntity(): APEntity.Tender.Address =
        APEntity.Tender.Address(
            streetAddress = this.streetAddress,
            postalCode = this.postalCode,
            addressDetails = this.addressDetails
                .let { addressDetails ->
                    APEntity.Tender.Address.AddressDetails(
                        country = addressDetails.country
                            .let { country ->
                                APEntity.Tender.Address.AddressDetails.Country(
                                    scheme = country.scheme,
                                    id = country.id,
                                    description = country.description,
                                    uri = country.uri
                                )
                            },
                        region = addressDetails.region
                            .let { region ->
                                APEntity.Tender.Address.AddressDetails.Region(
                                    scheme = region.scheme,
                                    id = region.id,
                                    description = region.description,
                                    uri = region.uri
                                )
                            },
                        locality = addressDetails.locality
                            ?.let { locality ->
                                APEntity.Tender.Address.AddressDetails.Locality(
                                    scheme = locality.scheme,
                                    id = locality.id,
                                    description = locality.description,
                                    uri = locality.uri
                                )
                            }
                    )
                }
        )

    private fun getEntity(tender: APEntity, entity: TenderProcessEntity, dateTime: LocalDateTime): TenderProcessEntity =
        TenderProcessEntity(
            cpId = entity.cpId,
            token = entity.token,
            stage = entity.stage,
            owner = entity.owner,
            createdDate = dateTime.toDate(),
            jsonData = toJson(tender)
        )
}
