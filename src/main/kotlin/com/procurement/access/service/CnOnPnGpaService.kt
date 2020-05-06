package com.procurement.access.service

import com.procurement.access.application.model.context.CheckCnOnPnGpaContext
import com.procurement.access.application.model.context.CreateCnOnPnGpaContext
import com.procurement.access.application.service.CheckedCnOnPnGpa
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.enums.CriteriaRelatesToEnum
import com.procurement.access.domain.model.enums.DocumentType
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.enums.MainProcurementCategory
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.exception.ErrorType.DATA_NOT_FOUND
import com.procurement.access.exception.ErrorType.INVALID_DOCS_ID
import com.procurement.access.exception.ErrorType.INVALID_DOCS_RELATED_LOTS
import com.procurement.access.exception.ErrorType.INVALID_ITEMS_RELATED_LOTS
import com.procurement.access.exception.ErrorType.INVALID_LOT_CONTRACT_PERIOD
import com.procurement.access.exception.ErrorType.INVALID_LOT_CURRENCY
import com.procurement.access.exception.ErrorType.INVALID_PMM
import com.procurement.access.exception.ErrorType.INVALID_PROCURING_ENTITY
import com.procurement.access.exception.ErrorType.INVALID_TENDER_AMOUNT
import com.procurement.access.exception.ErrorType.ITEM_ID_IS_DUPLICATED
import com.procurement.access.exception.ErrorType.LOT_ID_DUPLICATED
import com.procurement.access.infrastructure.dto.cn.CheckCnOnPnGpaRequest
import com.procurement.access.infrastructure.dto.cn.CreateCnOnPnGpaRequest
import com.procurement.access.infrastructure.dto.cn.CreateCnOnPnGpaResponse
import com.procurement.access.infrastructure.dto.cn.criteria.Period
import com.procurement.access.infrastructure.dto.cn.criteria.Requirement
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.lib.toSetBy
import com.procurement.access.lib.uniqueBy
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@Service
class CnOnPnGpaService(
    private val generationService: GenerationService,
    private val tenderProcessDao: TenderProcessDao,
    private val rulesService: RulesService
) {

    fun checkCnOnPnGpa(context: CheckCnOnPnGpaContext, data: CheckCnOnPnGpaRequest): CheckedCnOnPnGpa {
        val entity: TenderProcessEntity =
            tenderProcessDao.getByCpIdAndStage(context.cpid, context.previousStage)
                ?: throw ErrorException(DATA_NOT_FOUND)

        val pnEntity: PNEntity = toObject(PNEntity::class.java, entity.jsonData)

        //VR-3.8.18 Tender status
        checkTenderStatus(pnEntity)

        //VR-3.8.3 Documents (duplicate)
        checkDocuments(documentsFromRequest = data.tender.documents, documentsFromPN = pnEntity.tender.documents)

        data.tender.procuringEntity?.also { requestProcuringEntity ->
            //VR-1.0.1.10.1
            checkProcuringEntityIdentifier(requestProcuringEntity, pnEntity.tender.procuringEntity)

            // VR-1.0.1.10.2, VR-1.0.1.10.3, VR-1.0.1.10.6
            checkProcuringEntityPersones(requestProcuringEntity)

            // VR-1.0.1.10.4, VR-1.0.1.10.5
            checkPersonesBusinessFunctions(requestProcuringEntity)

            // VR-1.0.1.10.7
            checkBusinessFunctionPeriod(requestProcuringEntity, context)

            // VR-1.0.1.2.1, VR-1.0.1.2.7, VR-1.0.1.2.8
            checkBusinessFunctionDocuments(requestProcuringEntity)
            checkTenderDocumentsNotEmpty(data.tender)
        }


        data.tender.secondStage?.run {
            validateCompleteness()                      // VR-1.0.1.11.1

            minimumCandidates
                ?.let { validateMinimumCandidates(it) } // VR-1.0.1.11.2

            maximumCandidates
                ?.let { validateMaximumCandidates(it) } // VR-1.0.1.11.3

            // VR-1.0.1.11.4
            if (this.minimumCandidates != null && this.maximumCandidates != null)
                validateCandidatesRange(min = minimumCandidates, max = maximumCandidates)
        }



        if (pnEntity.tender.items.isEmpty()) {
            val lotsIdsFromRequest = data.tender.lots.asSequence()
                .map { it.id }
                .toSet()

            /** Begin check Lots */
            //VR-3.8.5(CN on PN)  "Currency" (lot)
            checkCurrencyInLotsFromRequest(
                lotsFromRequest = data.tender.lots,
                budgetFromPN = pnEntity.planning.budget
            )

            //VR-3.8.8(CN on PN)  "Contract Period" (Lot) -> VR-3.6.7(CN)
            checkContractPeriodInLotsWhenPNWithoutItemsFromRequest(tenderFromRequest = data)

            //VR-3.8.10(CN on PN) Lots (tender.lots) -> VR-3.6.9(CN)
            checkLotIdsAsRelatedLotInItems(
                lotsIdsFromRequest = lotsIdsFromRequest,
                itemsFromRequest = data.tender.items
            )
            //VR-3.8.12(CN on PN) Lot.ID -> VR-3.1.14(CN)
            checkLotIdFromRequest(lotsFromRequest = data.tender.lots)
            /** End check Lots */

            /** Begin check Items */
            //VR-3.8.9(CN on PN) "Quantity" (item) -> VR-3.6.11(CN)
            checkQuantityInItems(itemsFromRequest = data.tender.items)

            //VR-3.8.11(CN on PN) Items (tender.Items) -> VR-3.6.8(CN)
            checkRelatedLotInItemsFromRequest(
                lotsIdsFromRequest = lotsIdsFromRequest,
                itemsFromRequest = data.tender.items
            )

            //VR-3.8.13(CN on PN) Item.ID -> VR-3.1.15(CN)
            checkItemIdFromRequest(itemsFromRequest = data.tender.items)
            /** End check Items */

            /** Begin check Tender */
            //VR-3.8.4(CN on PN) "Value" (tender)
            val tenderValue = calculateTenderValueFromLots(lotsFromRequest = data.tender.lots)
            checkTenderValue(tenderValue.amount, pnEntity.planning.budget)

            //VR-3.8.6(CN on PN)  "Contract Period"(Tender) -> VR-3.6.10(CN)
            checkContractPeriodInTender(
                lotsFromRequest = data.tender.lots,
                budgetBreakdownsFromPN = pnEntity.planning.budget.budgetBreakdowns
            )
            /** End check Tender */

            /** Begin check Auctions */
            //VR-1.0.1.7.7
            checkAuctionsAreRequired(
                context = context,
                data = data,
                mainProcurementCategory = pnEntity.tender.mainProcurementCategory
            )
            /** End check Auctions */

            /** Begin check Documents*/
            //VR-3.8.7(CN on PN)  "Related Lots"(documents) -> VR-3.6.12(CN)
            checkRelatedLotsInDocumentsFromRequestWhenPNWithoutItems(
                lotsIdsFromRequest = lotsIdsFromRequest,
                documentsFromRequest = data.tender.documents
            )
            /** End check Documents */
        } else {
            /** Begin check Lots*/
            //VR-3.8.16 "Contract Period" (Lot)
            checkContractPeriodInLotsFromRequestWhenPNWithItems(
                preQualificationPeriodEndDate = data.preQualification.period.endDate,
                lotsFromPN = pnEntity.tender.lots
            )
            /** End check Lots */

            /** Begin check Auctions*/
            //VR-3.8.15 electronicAuctions.details
            checkAuctionsAreRequired(
                context = context,
                data = data,
                mainProcurementCategory = pnEntity.tender.mainProcurementCategory
            )
            /** End check Auctions */

            /** Begin check Documents*/
            //VR-3.8.17(CN on PN)  "Related Lots"(documents) -> VR-3.7.13(Update CNEntity)
            val lotsIdsFromPN = pnEntity.tender.lots.toSetBy { it.id }
            checkRelatedLotsInDocumentsFromRequestWhenPNWithItems(
                lotsIdsFromPN = lotsIdsFromPN,
                documentsFromRequest = data.tender.documents
            )
            /** End check Documents */
        }

        return CheckedCnOnPnGpa(requireAuction = data.tender.electronicAuctions != null)
    }

    fun createCnOnPnGpa(context: CreateCnOnPnGpaContext, data: CreateCnOnPnGpaRequest): CreateCnOnPnGpaResponse {
        val tenderProcessEntity = tenderProcessDao.getByCpIdAndStage(context.cpid, context.previousStage)
            ?: throw ErrorException(DATA_NOT_FOUND)

        val pnEntity: PNEntity = toObject(PNEntity::class.java, tenderProcessEntity.jsonData)

        val tender: CNEntity.Tender = if (pnEntity.tender.items.isEmpty())
            createTenderBasedPNWithoutItems(request = data, pnEntity = pnEntity)
        else
            createTenderBasedPNWithItems(request = data, pnEntity = pnEntity)

        val cnEntity = CNEntity(
            ocid = pnEntity.ocid,
            planning = planning(pnEntity), //BR-3.8.1
            tender = tender
        )

        tenderProcessDao.save(
            TenderProcessEntity(
                cpId = context.cpid,
                token = tenderProcessEntity.token,
                stage = context.stage,
                owner = tenderProcessEntity.owner,
                createdDate = context.startDate.toDate(),
                jsonData = toJson(cnEntity)
            )
        )

        return getResponse(cnEntity)
    }


    /** Begin Business Rules */
    private fun createTenderBasedPNWithoutItems(
        request: CreateCnOnPnGpaRequest,
        pnEntity: PNEntity
    ): CNEntity.Tender {
        //BR-3.6.5
        val relatedTemporalWithPermanentLotId: Map<String, String> = generatePermanentLotId(request.tender.lots)
        val relatedTemporalWithPermanentItemId: Map<String, String> = generatePermanentItemId(request.tender.items)

        /** Begin BR-3.8.3 */
        val classification: CNEntity.Tender.Classification =
            classificationFromRequest(classificationFromRequest = request.tender.classification!!)
        val lots: List<CNEntity.Tender.Lot> = convertRequestLots(request.tender, relatedTemporalWithPermanentLotId)
        val items: List<CNEntity.Tender.Item> =
            convertRequestItems(
                request.tender.items,
                relatedTemporalWithPermanentLotId,
                relatedTemporalWithPermanentItemId
            )
        /** End BR-3.8.3 */

        val rawCriteria = criteriaFromRequest(criteriaFromRequest = request.tender.criteria)
        val conversions = conversionsFromRequest(conversionsFromRequest = request.tender.conversions)
        if ((rawCriteria == null) && (conversions != null))
            throw ErrorException(ErrorType.CONVERSIONS_IS_EMPTY)

        val criteria = rawCriteria?.let {
            convertRequestCriteria(
                tender = request.tender,
                relatedTemporalWithPermanentItemId = relatedTemporalWithPermanentItemId,
                relatedTemporalWithPermanentLotId = relatedTemporalWithPermanentLotId
            )
        }

        /** Begin BR-3.8.4 */
        //BR-3.8.14 -> BR-3.6.30
        val value: CNEntity.Tender.Value = calculateTenderValueFromLotsGpaCheck(request.tender.lots)
        //BR-3.8.15 -> BR-3.6.31
        val contractPeriod: CNEntity.Tender.ContractPeriod = calculationTenderContractPeriodGpaCreate(lots = request.tender.lots)
        /** End BR-3.8.4 */

        //BR-3.8.5 -> BR-3.6.5
        val electronicAuctions: CNEntity.Tender.ElectronicAuctions? = convertElectronicAuctionsFromRequest(
            tenderFromRequest = request.tender,
            relatedTemporalWithPermanentLotId = relatedTemporalWithPermanentLotId
        )

        //BR-3.8.18 -> BR-3.7.13
        val updatedDocuments: List<CNEntity.Tender.Document> = updateDocuments(
            documentsFromRequest = request.tender.documents,
            documentsFromDB = pnEntity.tender.documents ?: emptyList(),
            relatedTemporalWithPermanentLotId = relatedTemporalWithPermanentLotId
        )

        return tender(
            request = request,
            pnEntity = pnEntity,
            classification = classification,
            criteria = criteria,
            conversions = conversions,
            lots = lots,
            items = items,
            value = value,
            contractPeriod = contractPeriod,
            electronicAuctions = electronicAuctions,
            updatedDocuments = updatedDocuments
        )
    }

    private fun createTenderBasedPNWithItems(
        request: CreateCnOnPnGpaRequest,
        pnEntity: PNEntity
    ): CNEntity.Tender {
        /** Begin BR-3.8.3 */
        val classification: CNEntity.Tender.Classification =
            classificationFromPNToCN(classificationFromPN = pnEntity.tender.classification)
        val lots: List<CNEntity.Tender.Lot> = lotsFromPNToCN(lotsFromPN = pnEntity.tender.lots)
        val items: List<CNEntity.Tender.Item> = itemsFromPNToCN(itemsFromPN = pnEntity.tender.items)
        /** End BR-3.8.3 */

        val criteria = criteriaFromRequest(criteriaFromRequest = request.tender.criteria)
        val conversions = conversionsFromRequest(conversionsFromRequest = request.tender.conversions)
        if ((criteria == null) && (conversions != null)) throw ErrorException(ErrorType.CONVERSIONS_IS_EMPTY)

        /** Begin BR-3.8.4 */
        val value: CNEntity.Tender.Value = pnEntity.tender.value.let {
            CNEntity.Tender.Value(
                amount = it.amount,
                currency = it.currency
            )
        }
        val contractPeriod: CNEntity.Tender.ContractPeriod = pnEntity.tender.contractPeriod!!.let {
            CNEntity.Tender.ContractPeriod(
                startDate = it.startDate,
                endDate = it.endDate
            )
        }
        /** End BR-3.8.4 */

        //BR-3.8.5 -> BR-3.6.5
        val electronicAuctions: CNEntity.Tender.ElectronicAuctions? = convertElectronicAuctionsFromRequest(
            tenderFromRequest = request.tender
        )

        //BR-3.8.18 -> BR-3.7.13
        val updatedDocuments: List<CNEntity.Tender.Document> = updateDocuments(
            documentsFromRequest = request.tender.documents,
            documentsFromDB = pnEntity.tender.documents ?: emptyList(),
            relatedTemporalWithPermanentLotId = emptyMap()
        )

        return tender(
            request = request,
            pnEntity = pnEntity,
            classification = classification,
            criteria = criteria,
            conversions = conversions,
            lots = lots,
            items = items,
            value = value,
            contractPeriod = contractPeriod,
            electronicAuctions = electronicAuctions,
            updatedDocuments = updatedDocuments
        )
    }


    private fun convertRequestItems(
        itemsFromRequest: List<CreateCnOnPnGpaRequest.Tender.Item>,
        relatedTemporalWithPermanentLotId: Map<String, String>,
        relatedTemporalWithPermanentItemId: Map<String, String>
    ): List<CNEntity.Tender.Item> {
        return itemsFromRequest.map { item ->
            CNEntity.Tender.Item(
                //BR-3.8.6(CN on PN) item id (tender.items.id) -> BR-3.6.6
                internalId = item.internalId,
                id = relatedTemporalWithPermanentItemId.getValue(item.id),
                description = item.description,
                classification = item.classification.let { classification ->
                    CNEntity.Tender.Item.Classification(
                        scheme = classification.scheme,
                        id = classification.id,
                        description = classification.description,
                        uri = null
                    )
                },
                additionalClassifications = item.additionalClassifications
                    ?.map { additionalClassification ->
                        CNEntity.Tender.Item.AdditionalClassification(
                            scheme = additionalClassification.scheme,
                            id = additionalClassification.id,
                            description = additionalClassification.description
                        )
                    },
                quantity = item.quantity,
                unit = item.unit.let { unit ->
                    CNEntity.Tender.Item.Unit(
                        id = unit.id,
                        name = unit.name
                    )
                },
                relatedLot = relatedTemporalWithPermanentLotId.getValue(item.relatedLot) //BR-3.8.6(CN on PN) -> BR-3.6.5(CN)
            )
        }
    }

    /**
     * BR-3.8.3
     */
    private fun convertRequestLots(
        tender: CreateCnOnPnGpaRequest.Tender,
        relatedTemporalWithPermanentLotId: Map<String, String>
    ): List<CNEntity.Tender.Lot> {
        return tender.lots.map { lot ->
            CNEntity.Tender.Lot(
                id = relatedTemporalWithPermanentLotId.getValue(lot.id), //BR-3.8.5
                internalId = lot.internalId,
                title = lot.title,
                description = lot.description,
                /** Begin BR-3.8.7 -> BR-3.6.1 */
                status = LotStatus.ACTIVE,
                statusDetails = LotStatusDetails.EMPTY,
                /** End BR-3.8.7 -> BR-3.6.1 */

                //BR-3.8.4; BR-3.8.14 -> BR-3.6.30
                value = lot.value.let { value ->
                    CNEntity.Tender.Lot.Value(
                        amount = value.amount,
                        currency = value.currency
                    )
                },

                /** Begin BR-3.8.4 */
                //BR-3.8.9 -> BR-3.6.17
                options = listOf(CNEntity.Tender.Lot.Option(false)), //BR-3.8.4; BR-3.8.9 -> BR-3.6.17
                //BR-3.8.10 -> BR-3.6.18
                variants = listOf(CNEntity.Tender.Lot.Variant(false)), //BR-3.8.4; BR-3.8.10 -> BR-3.6.18
                //BR-3.8.11 -> BR-3.6.19
                renewals = listOf(CNEntity.Tender.Lot.Renewal(false)), //BR-3.8.4; BR-3.8.11 -> BR-3.6.19
                //BR-3.8.12 -> BR-3.6.20
                recurrentProcurement = listOf(CNEntity.Tender.Lot.RecurrentProcurement(false)), //BR-3.8.4; BR-3.8.12 -> BR-3.6.20
                /** End BR-3.8.4 */

                //BR-3.8.4; BR-3.8.15 -> BR-3.6.31
                contractPeriod = lot.contractPeriod.let { contractPeriod ->
                    CNEntity.Tender.Lot.ContractPeriod(
                        startDate = contractPeriod.startDate,
                        endDate = contractPeriod.endDate
                    )
                },
                placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                    CNEntity.Tender.Lot.PlaceOfPerformance(
                        address = placeOfPerformance.address.let { address ->
                            CNEntity.Tender.Lot.PlaceOfPerformance.Address(
                                streetAddress = address.streetAddress,
                                postalCode = address.postalCode,
                                addressDetails = address.addressDetails.let { addressDetails ->
                                    CNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                        country = addressDetails.country.let { country ->
                                            CNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                scheme = country.scheme!!, //VR-3.14.1(CheckItem)
                                                id = country.id,
                                                description = country.description!!,
                                                uri = country.uri!!
                                            )
                                        },
                                        region = addressDetails.region.let { region ->
                                            CNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                scheme = region.scheme!!,
                                                id = region.id,
                                                description = region.description!!,
                                                uri = region.uri!!
                                            )
                                        },
                                        locality = addressDetails.locality.let { locality ->
                                            CNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
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
                        description = placeOfPerformance.description
                    )
                }
            )
        }
    }

    /**
     * BR-3.8.3
     */
    private fun classificationFromRequest(
        classificationFromRequest: CreateCnOnPnGpaRequest.Tender.Classification
    ): CNEntity.Tender.Classification {
        return classificationFromRequest.let {
            CNEntity.Tender.Classification(
                scheme = it.scheme,
                id = it.id,
                description = it.description,
                uri = null
            )
        }
    }

    private fun convertRequestCriteria(
        tender: CreateCnOnPnGpaRequest.Tender,
        relatedTemporalWithPermanentLotId: Map<String, String>,
        relatedTemporalWithPermanentItemId: Map<String, String>
    ): List<CNEntity.Tender.Criteria> {
        return tender.criteria!!.map { criteria ->
            CNEntity.Tender.Criteria(
                id = criteria.id,
                title = criteria.title,
                description = criteria.description,
                requirementGroups = criteria.requirementGroups.map { requirementGroup ->
                    CNEntity.Tender.Criteria.RequirementGroup(
                        id = requirementGroup.id,
                        description = requirementGroup.description,
                        requirements = requirementGroup.requirements.map { requirement ->
                            Requirement(
                                id = requirement.id,
                                description = requirement.description,
                                title = requirement.title,
                                period = requirement.period?.let { period ->
                                    Period(
                                        startDate = period.startDate,
                                        endDate = period.endDate
                                    )
                                },
                                dataType = requirement.dataType,
                                value = requirement.value
                            )
                        }
                    )
                },
                relatesTo = criteria.relatesTo,
                relatedItem = when (criteria.relatesTo) {
                    CriteriaRelatesToEnum.LOT  -> relatedTemporalWithPermanentLotId.getValue(criteria.relatedItem!!)
                    CriteriaRelatesToEnum.ITEM -> relatedTemporalWithPermanentItemId.getValue(criteria.relatedItem!!)
                    else                       -> criteria.relatedItem
                }
            )
        }
    }

    /**
     * BR-3.8.3
     */
    private fun itemsFromPNToCN(itemsFromPN: List<PNEntity.Tender.Item>): List<CNEntity.Tender.Item> {
        return itemsFromPN.map { item ->
            CNEntity.Tender.Item(
                //BR-3.8.6
                id = item.id,
                internalId = item.internalId,
                description = item.description,
                classification = item.classification.let { classification ->
                    CNEntity.Tender.Item.Classification(
                        scheme = classification.scheme,
                        id = classification.id,
                        description = classification.description,
                        uri = null
                    )
                },
                additionalClassifications = item.additionalClassifications
                    ?.map { additionalClassification ->
                        CNEntity.Tender.Item.AdditionalClassification(
                            scheme = additionalClassification.scheme,
                            id = additionalClassification.id,
                            description = additionalClassification.description
                        )
                    },
                quantity = item.quantity,
                unit = item.unit.let { unit ->
                    CNEntity.Tender.Item.Unit(
                        id = unit.id,
                        name = unit.name
                    )
                },
                relatedLot = item.relatedLot
            )
        }
    }

    /**
     * BR-3.8.3
     */
    private fun lotsFromPNToCN(lotsFromPN: List<PNEntity.Tender.Lot>): List<CNEntity.Tender.Lot> {
        return lotsFromPN.map { lot ->
            /** Begin BR-3.8.7 */
            val status = if (lot.status == LotStatus.PLANNING)
                LotStatus.ACTIVE
            else
                lot.status
            /** End BR-3.8.7 */

            CNEntity.Tender.Lot(
                //BR-3.8.5
                id = lot.id,

                internalId = lot.internalId,
                title = lot.title,
                description = lot.description,
                /** Begin BR-3.8.7 */
                status = status,
                statusDetails = LotStatusDetails.EMPTY,
                /** End BR-3.8.7 */
                value = lot.value.let { value ->
                    CNEntity.Tender.Lot.Value(
                        amount = value.amount,
                        currency = value.currency
                    )
                },
                options = listOf(CNEntity.Tender.Lot.Option(false)), //BR-3.8.9 -> BR-3.6.17
                recurrentProcurement = listOf(CNEntity.Tender.Lot.RecurrentProcurement(false)), //BR-3.8.12 -> BR-3.6.20
                renewals = listOf(CNEntity.Tender.Lot.Renewal(false)), //BR-3.8.11 -> BR-3.6.19
                variants = listOf(CNEntity.Tender.Lot.Variant(false)), //BR-3.8.10 -> BR-3.6.18
                contractPeriod = lot.contractPeriod.let { contractPeriod ->
                    CNEntity.Tender.Lot.ContractPeriod(
                        startDate = contractPeriod.startDate,
                        endDate = contractPeriod.endDate
                    )
                },
                placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                    CNEntity.Tender.Lot.PlaceOfPerformance(
                        address = placeOfPerformance.address.let { address ->
                            CNEntity.Tender.Lot.PlaceOfPerformance.Address(
                                streetAddress = address.streetAddress,
                                postalCode = address.postalCode,
                                addressDetails = address.addressDetails.let { addressDetails ->
                                    CNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                        country = addressDetails.country.let { country ->
                                            CNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                scheme = country.scheme, //VR-3.14.1(CheckItem)
                                                id = country.id,
                                                description = country.description,
                                                uri = country.uri
                                            )
                                        },
                                        region = addressDetails.region.let { region ->
                                            CNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                scheme = region.scheme,
                                                id = region.id,
                                                description = region.description,
                                                uri = region.uri
                                            )
                                        },
                                        locality = addressDetails.locality.let { locality ->
                                            CNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
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
                        description = placeOfPerformance.description
                    )
                }
            )
        }
    }


    /**
     * BR-3.8.3
     */
    private fun classificationFromPNToCN(
        classificationFromPN: PNEntity.Tender.Classification
    ): CNEntity.Tender.Classification {
        return classificationFromPN.let {
            CNEntity.Tender.Classification(
                scheme = it.scheme,
                id = it.id,
                description = it.description,
                uri = null
            )
        }
    }

    private fun criteriaFromRequest(
        criteriaFromRequest: List<CreateCnOnPnGpaRequest.Tender.Criteria>?
    ): List<CNEntity.Tender.Criteria>? {
        return criteriaFromRequest?.map { criteria ->
            CNEntity.Tender.Criteria(
                id = criteria.id,
                title = criteria.title,
                description = criteria.description,
                requirementGroups = criteria.requirementGroups.map { requirementGroup ->
                    CNEntity.Tender.Criteria.RequirementGroup(
                        id = requirementGroup.id,
                        description = requirementGroup.description,
                        requirements = requirementGroup.requirements.map { requirement ->
                            Requirement(
                                id = requirement.id,
                                description = requirement.description,
                                title = requirement.title,
                                period = requirement.period?.let { period ->
                                    Period(
                                        startDate = period.startDate,
                                        endDate = period.endDate
                                    )
                                },
                                dataType = requirement.dataType,
                                value = requirement.value
                            )
                        }
                    )
                },
                relatesTo = criteria.relatesTo,
                relatedItem = criteria.relatedItem
            )
        }
    }

    private fun updateDocuments(
        documentsFromRequest: List<CreateCnOnPnGpaRequest.Tender.Document>,
        documentsFromDB: List<PNEntity.Tender.Document>,
        relatedTemporalWithPermanentLotId: Map<String, String>
    ): List<CNEntity.Tender.Document> {
        return if (documentsFromDB.isNotEmpty()) {
            val documentsFromRequestById: Map<String, CreateCnOnPnGpaRequest.Tender.Document> =
                documentsFromRequest.associateBy { document -> document.id }
            val existsDocumentsById: Map<String, PNEntity.Tender.Document> =
                documentsFromDB.associateBy { document -> document.id }

            val updatedDocuments: Set<CNEntity.Tender.Document> = updateExistsDocuments(
                documentsFromRequestById = documentsFromRequestById,
                existsDocumentsById = existsDocumentsById,
                relatedTemporalWithPermanentLotId = relatedTemporalWithPermanentLotId
            )

            val newDocumentsFromRequest: Set<CreateCnOnPnGpaRequest.Tender.Document> = extractNewDocuments(
                documentsFromRequest = documentsFromRequest,
                existsDocumentsById = existsDocumentsById
            )

            val newDocuments: List<CNEntity.Tender.Document> = convertNewDocuments(
                newDocumentsFromRequest = newDocumentsFromRequest,
                relatedTemporalWithPermanentLotId = relatedTemporalWithPermanentLotId
            )

            updatedDocuments.union(newDocuments).toList()
        } else {
            convertNewDocuments(
                newDocumentsFromRequest = documentsFromRequest,
                relatedTemporalWithPermanentLotId = relatedTemporalWithPermanentLotId
            )
        }
    }

    private fun updateExistsDocuments(
        documentsFromRequestById: Map<String, CreateCnOnPnGpaRequest.Tender.Document>,
        existsDocumentsById: Map<String, PNEntity.Tender.Document>,
        relatedTemporalWithPermanentLotId: Map<String, String>
    ): Set<CNEntity.Tender.Document> {
        return existsDocumentsById.asSequence()
            .map { (id, document) ->
                val documentSource =
                    documentsFromRequestById[id]
                        ?: throw ErrorException(
                            error = INVALID_DOCS_ID,
                            message = "Document with id: '$id' from db not contains in request"
                        )

                val relatedLots = getPermanentLotsIds(
                    temporalIds = documentSource.relatedLots,
                    relatedTemporalWithPermanentLotId = relatedTemporalWithPermanentLotId
                )

                CNEntity.Tender.Document(
                    documentType = document.documentType,
                    id = document.id,
                    title = documentSource.title,
                    description = documentSource.description,
                    //BR-3.6.5(CN)
                    relatedLots = relatedLots
                )
            }.toSet()
    }

    private fun getPermanentLotsIds(
        temporalIds: List<String>?,
        relatedTemporalWithPermanentLotId: Map<String, String>
    ): List<String>? {
        return if (temporalIds != null && relatedTemporalWithPermanentLotId.isNotEmpty())
            temporalIds.map { relatedTemporalWithPermanentLotId.getValue(it) }
        else
            temporalIds
    }

    private fun extractNewDocuments(
        documentsFromRequest: Collection<CreateCnOnPnGpaRequest.Tender.Document>,
        existsDocumentsById: Map<String, PNEntity.Tender.Document>
    ): Set<CreateCnOnPnGpaRequest.Tender.Document> {
        return documentsFromRequest.asSequence()
            .filter { document -> !existsDocumentsById.containsKey(document.id) }
            .toSet()
    }

    private fun convertNewDocuments(
        newDocumentsFromRequest: Collection<CreateCnOnPnGpaRequest.Tender.Document>,
        relatedTemporalWithPermanentLotId: Map<String, String>
    ): List<CNEntity.Tender.Document> {
        return newDocumentsFromRequest.map { document ->
            convertNewDocument(document, relatedTemporalWithPermanentLotId)
        }
    }

    private fun convertNewDocument(
        newDocumentFromRequest: CreateCnOnPnGpaRequest.Tender.Document,
        relatedTemporalWithPermanentLotId: Map<String, String>
    ): CNEntity.Tender.Document {
        val relatedLots = getPermanentLotsIds(
            temporalIds = newDocumentFromRequest.relatedLots,
            relatedTemporalWithPermanentLotId = relatedTemporalWithPermanentLotId
        )

        return CNEntity.Tender.Document(
            id = newDocumentFromRequest.id,
            documentType = DocumentType.creator(newDocumentFromRequest.documentType.key),
            title = newDocumentFromRequest.title,
            description = newDocumentFromRequest.description,
            //BR-3.6.5(CN)
            relatedLots = relatedLots
        )
    }

    private fun tender(
        request: CreateCnOnPnGpaRequest,
        pnEntity: PNEntity,
        classification: CNEntity.Tender.Classification,
        criteria: List<CNEntity.Tender.Criteria>?,
        conversions: List<CNEntity.Tender.Conversion>?,
        lots: List<CNEntity.Tender.Lot>,
        items: List<CNEntity.Tender.Item>,
        value: CNEntity.Tender.Value,
        contractPeriod: CNEntity.Tender.ContractPeriod,
        electronicAuctions: CNEntity.Tender.ElectronicAuctions?,
        updatedDocuments: List<CNEntity.Tender.Document>
    ): CNEntity.Tender {
        /** Begin BR-3.8.8(CN on PN) Status StatusDetails (tender) -> BR-3.6.2(CN)*/
        val status = TenderStatus.ACTIVE
        val statusDetails: TenderStatusDetails = TenderStatusDetails.SUBMISSION
        /** End BR-3.8.8(CN on PN) Status StatusDetails (tender) -> BR-3.6.2(CN)*/

        return CNEntity.Tender(
            id = generationService.generatePermanentTenderId(),
            /** Begin BR-3.8.8 -> BR-3.6.2*/
            status = status,
            statusDetails = statusDetails,
            /** End BR-3.8.8 -> BR-3.6.2*/

            classification = classification,
            title = pnEntity.tender.title, //BR-3.8.1
            description = pnEntity.tender.description, //BR-3.8.1
            //BR-3.8.1
            acceleratedProcedure = pnEntity.tender.acceleratedProcedure.let {
                CNEntity.Tender.AcceleratedProcedure(
                    isAcceleratedProcedure = it.isAcceleratedProcedure
                )
            },
            //BR-3.8.1
            designContest = pnEntity.tender.designContest.let {
                CNEntity.Tender.DesignContest(
                    serviceContractAward = it.serviceContractAward
                )
            },
            //BR-3.8.1
            electronicWorkflows = pnEntity.tender.electronicWorkflows.let {
                CNEntity.Tender.ElectronicWorkflows(
                    useOrdering = it.useOrdering,
                    usePayment = it.usePayment,
                    acceptInvoicing = it.acceptInvoicing
                )
            },
            //BR-3.8.1
            jointProcurement = pnEntity.tender.jointProcurement.let {
                CNEntity.Tender.JointProcurement(
                    isJointProcurement = it.isJointProcurement
                )
            },
            //BR-3.8.1
            procedureOutsourcing = pnEntity.tender.procedureOutsourcing.let {
                CNEntity.Tender.ProcedureOutsourcing(
                    procedureOutsourced = it.procedureOutsourced
                )
            },
            //BR-3.8.1
            framework = pnEntity.tender.framework.let {
                CNEntity.Tender.Framework(
                    isAFramework = it.isAFramework
                )
            },
            //BR-3.8.1
            dynamicPurchasingSystem = pnEntity.tender.dynamicPurchasingSystem.let {
                CNEntity.Tender.DynamicPurchasingSystem(
                    hasDynamicPurchasingSystem = it.hasDynamicPurchasingSystem
                )
            },
            legalBasis = pnEntity.tender.legalBasis, //BR-3.8.1
            procurementMethod = pnEntity.tender.procurementMethod, //BR-3.8.1
            procurementMethodDetails = pnEntity.tender.procurementMethodDetails,//BR-3.8.1
            procurementMethodRationale = request.tender.procurementMethodRationale,
            procurementMethodAdditionalInfo = request.tender.procurementMethodAdditionalInfo,
            mainProcurementCategory = pnEntity.tender.mainProcurementCategory, //BR-3.8.1

            eligibilityCriteria = pnEntity.tender.eligibilityCriteria, //BR-3.8.1

            //BR-3.8.17 -> BR-3.6.22 | VR-3.6.16
            awardCriteria = request.tender.awardCriteria,
            awardCriteriaDetails = request.tender.awardCriteriaDetails,
            tenderPeriod = null,
            secondStage = request.tender.secondStage
                ?.let { secondStage ->
                    CNEntity.Tender.SecondStage(
                        minimumCandidates = secondStage.minimumCandidates,
                        maximumCandidates = secondStage.maximumCandidates
                    )
                },
            contractPeriod = contractPeriod,
            enquiryPeriod = null,
            procurementMethodModalities = request.tender.procurementMethodModalities,
            electronicAuctions = electronicAuctions, //BR-3.8.5 -> BR-3.6.5
            //BR-3.8.1
            procuringEntity = pnEntity.tender.procuringEntity.let { procuringEntity ->
                CNEntity.Tender.ProcuringEntity(
                    id = procuringEntity.id,
                    name = procuringEntity.name,
                    identifier = procuringEntity.identifier.let { identifier ->
                        CNEntity.Tender.ProcuringEntity.Identifier(
                            scheme = identifier.scheme,
                            id = identifier.id,
                            legalName = identifier.legalName,
                            uri = identifier.uri
                        )
                    },
                    additionalIdentifiers = procuringEntity.additionalIdentifiers?.map { additionalIdentifier ->
                        CNEntity.Tender.ProcuringEntity.AdditionalIdentifier(
                            scheme = additionalIdentifier.scheme,
                            id = additionalIdentifier.id,
                            legalName = additionalIdentifier.legalName,
                            uri = additionalIdentifier.uri
                        )
                    },
                    address = procuringEntity.address.let { address ->
                        CNEntity.Tender.ProcuringEntity.Address(
                            streetAddress = address.streetAddress,
                            postalCode = address.postalCode,
                            addressDetails = address.addressDetails.let { addressDetails ->
                                CNEntity.Tender.ProcuringEntity.Address.AddressDetails(
                                    country = addressDetails.country.let { country ->
                                        CNEntity.Tender.ProcuringEntity.Address.AddressDetails.Country(
                                            scheme = country.scheme,
                                            id = country.id,
                                            description = country.description,
                                            uri = country.uri
                                        )
                                    },
                                    region = addressDetails.region.let { region ->
                                        CNEntity.Tender.ProcuringEntity.Address.AddressDetails.Region(
                                            scheme = region.scheme,
                                            id = region.id,
                                            description = region.description,
                                            uri = region.uri
                                        )
                                    },
                                    locality = addressDetails.locality.let { locality ->
                                        CNEntity.Tender.ProcuringEntity.Address.AddressDetails.Locality(
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
                    contactPoint = procuringEntity.contactPoint.let { contactPoint ->
                        CNEntity.Tender.ProcuringEntity.ContactPoint(
                            name = contactPoint.name,
                            email = contactPoint.email,
                            telephone = contactPoint.telephone,
                            faxNumber = contactPoint.faxNumber,
                            url = contactPoint.url
                        )
                    },
                    persones = request.tender.procuringEntity?.let { _procuringEntity ->
                        _procuringEntity.persones?.map { person ->
                            CNEntity.Tender.ProcuringEntity.Persone(
                                title = person.title,
                                name = person.name,
                                identifier = CNEntity.Tender.ProcuringEntity.Persone.Identifier(
                                    scheme = person.identifier.scheme,
                                    id = person.identifier.id,
                                    uri = person.identifier.uri
                                ),
                                businessFunctions = person.businessFunctions.map { businessFunction ->
                                    CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction(
                                        id = businessFunction.id,
                                        jobTitle = businessFunction.jobTitle,
                                        type = businessFunction.type,
                                        period = CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Period(
                                            startDate = businessFunction.period.startDate
                                        ),
                                        documents = businessFunction.documents?.map { document ->
                                            CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document(
                                                id = document.id,
                                                documentType = document.documentType,
                                                title = document.title,
                                                description = document.description
                                            )
                                        }
                                    )
                                }
                            )
                        }
                    }
                )
            },
            value = value,
            //BR-3.8.1
            lotGroups = pnEntity.tender.lotGroups.map {
                CNEntity.Tender.LotGroup(
                    optionToCombine = it.optionToCombine
                )
            },
            requiresElectronicCatalogue = pnEntity.tender.requiresElectronicCatalogue,
            criteria = criteria,
            conversions = conversions,
            lots = lots, //BR-3.8.3
            items = items, //BR-3.8.3
            submissionMethod = pnEntity.tender.submissionMethod, //BR-3.8.1
            submissionMethodRationale = pnEntity.tender.submissionMethodRationale, //BR-3.8.1
            submissionMethodDetails = pnEntity.tender.submissionMethodDetails, //BR-3.8.1
            documents = updatedDocuments //BR-3.7.13
        )
    }

    private fun conversionsFromRequest(
        conversionsFromRequest: List<CreateCnOnPnGpaRequest.Tender.Conversion>?
    ): List<CNEntity.Tender.Conversion>? {
        return conversionsFromRequest?.map { conversion ->
            CNEntity.Tender.Conversion(
                id = conversion.id,
                relatedItem = conversion.relatedItem,
                relatesTo = conversion.relatesTo,
                rationale = conversion.rationale,
                description = conversion.description,
                coefficients = conversion.coefficients.map { coefficient ->
                    CNEntity.Tender.Conversion.Coefficient(
                        id = coefficient.id,
                        value = coefficient.value,
                        coefficient = coefficient.coefficient
                    )
                }
            )
        }
    }

    private fun convertElectronicAuctionsFromRequest(
        tenderFromRequest: CreateCnOnPnGpaRequest.Tender,
        relatedTemporalWithPermanentLotId: Map<String, String> = emptyMap()
    ): CNEntity.Tender.ElectronicAuctions? {
        return tenderFromRequest.electronicAuctions?.let {
            val details = it.details.map { detail ->
                CNEntity.Tender.ElectronicAuctions.Detail(
                    id = detail.id,
                    relatedLot = if (relatedTemporalWithPermanentLotId.isNotEmpty())
                        relatedTemporalWithPermanentLotId.getValue(detail.relatedLot) //BR-3.8.6(CN on PN) -> BR-3.6.5(CN)
                    else
                        detail.relatedLot,
                    electronicAuctionModalities = detail.electronicAuctionModalities.map { electronicAuctionModalities ->
                        CNEntity.Tender.ElectronicAuctions.Detail.Modalities(
                            eligibleMinimumDifference = electronicAuctionModalities.eligibleMinimumDifference.let { eligibleMinimumDifference ->
                                CNEntity.Tender.ElectronicAuctions.Detail.Modalities.EligibleMinimumDifference(
                                    amount = eligibleMinimumDifference.amount,
                                    currency = eligibleMinimumDifference.currency
                                )
                            }
                        )
                    }
                )
            }

            CNEntity.Tender.ElectronicAuctions(
                details = details
            )
        }
    }

    /**
     * BR-3.8.5(CN on PN) lot id (tender.lots.id) -> BR-3.6.5
     *
     * eAccess меняет временные "ID" (tender/lot/id) лотов на постоянные.
     * Постоянные "ID" (tender/lot/id) лотов формируются как уникальные для данного контрактного процесса
     * 32-символьные идентификаторы.
     */
    private fun generatePermanentLotId(lots: List<CreateCnOnPnGpaRequest.Tender.Lot>): Map<String, String> {
        return lots.asSequence()
            .map { lot ->
                val permanentId = generationService.generatePermanentLotId() //BR-3.8.6
                lot.id to permanentId
            }
            .toMap()
    }

    private fun generatePermanentItemId(itemsFromRequest: List<CreateCnOnPnGpaRequest.Tender.Item>): Map<String, String> {
        return itemsFromRequest.asSequence()
            .map { item ->
                val permanentId = generationService.generatePermanentItemId()
                item.id to permanentId
            }
            .toMap()
    }

    private fun getResponse(cn: CNEntity): CreateCnOnPnGpaResponse {
        return CreateCnOnPnGpaResponse(
            planning = cn.planning.let { planning ->
                CreateCnOnPnGpaResponse.Planning(
                    rationale = planning.rationale,
                    budget = planning.budget.let { budget ->
                        CreateCnOnPnGpaResponse.Planning.Budget(
                            description = budget.description,
                            amount = budget.amount.let { amount ->
                                CreateCnOnPnGpaResponse.Planning.Budget.Amount(
                                    amount = amount.amount,
                                    currency = amount.currency
                                )
                            },
                            isEuropeanUnionFunded = budget.isEuropeanUnionFunded,
                            budgetBreakdowns = budget.budgetBreakdowns.map { budgetBreakdown ->
                                CreateCnOnPnGpaResponse.Planning.Budget.BudgetBreakdown(
                                    id = budgetBreakdown.id,
                                    description = budgetBreakdown.description,
                                    amount = budgetBreakdown.amount.let { amount ->
                                        CreateCnOnPnGpaResponse.Planning.Budget.BudgetBreakdown.Amount(
                                            amount = amount.amount,
                                            currency = amount.currency
                                        )
                                    },
                                    period = budgetBreakdown.period.let { period ->
                                        CreateCnOnPnGpaResponse.Planning.Budget.BudgetBreakdown.Period(
                                            startDate = period.startDate,
                                            endDate = period.endDate
                                        )
                                    },
                                    sourceParty = budgetBreakdown.sourceParty.let { sourceParty ->
                                        CreateCnOnPnGpaResponse.Planning.Budget.BudgetBreakdown.SourceParty(
                                            id = sourceParty.id,
                                            name = sourceParty.name
                                        )
                                    },
                                    europeanUnionFunding = budgetBreakdown.europeanUnionFunding?.let { europeanUnionFunding ->
                                        CreateCnOnPnGpaResponse.Planning.Budget.BudgetBreakdown.EuropeanUnionFunding(
                                            projectIdentifier = europeanUnionFunding.projectIdentifier,
                                            projectName = europeanUnionFunding.projectName,
                                            uri = europeanUnionFunding.uri
                                        )
                                    }
                                )
                            }
                        )
                    }
                )
            },
            tender = cn.tender.let { tender ->
                CreateCnOnPnGpaResponse.Tender(
                    id = tender.id,
                    status = tender.status,
                    statusDetails = tender.statusDetails,
                    title = tender.title,
                    description = tender.description,
                    classification = tender.classification.let { classification ->
                        CreateCnOnPnGpaResponse.Tender.Classification(
                            scheme = classification.scheme,
                            id = classification.id,
                            description = classification.description
                        )
                    },
                    requiresElectronicCatalogue = tender.requiresElectronicCatalogue,
                    acceleratedProcedure = tender.acceleratedProcedure.let { acceleratedProcedure ->
                        CreateCnOnPnGpaResponse.Tender.AcceleratedProcedure(
                            isAcceleratedProcedure = acceleratedProcedure.isAcceleratedProcedure
                        )
                    },
                    designContest = tender.designContest.let { designContest ->
                        CreateCnOnPnGpaResponse.Tender.DesignContest(
                            serviceContractAward = designContest.serviceContractAward
                        )
                    },
                    electronicWorkflows = tender.electronicWorkflows.let { electronicWorkflows ->
                        CreateCnOnPnGpaResponse.Tender.ElectronicWorkflows(
                            useOrdering = electronicWorkflows.useOrdering,
                            usePayment = electronicWorkflows.usePayment,
                            acceptInvoicing = electronicWorkflows.acceptInvoicing
                        )
                    },
                    jointProcurement = tender.jointProcurement.let { jointProcurement ->
                        CreateCnOnPnGpaResponse.Tender.JointProcurement(
                            isJointProcurement = jointProcurement.isJointProcurement
                        )
                    },
                    procedureOutsourcing = tender.procedureOutsourcing.let { procedureOutsourcing ->
                        CreateCnOnPnGpaResponse.Tender.ProcedureOutsourcing(
                            procedureOutsourced = procedureOutsourcing.procedureOutsourced
                        )
                    },
                    framework = tender.framework.let { framework ->
                        CreateCnOnPnGpaResponse.Tender.Framework(
                            isAFramework = framework.isAFramework
                        )
                    },
                    dynamicPurchasingSystem = tender.dynamicPurchasingSystem.let { dynamicPurchasingSystem ->
                        CreateCnOnPnGpaResponse.Tender.DynamicPurchasingSystem(
                            hasDynamicPurchasingSystem = dynamicPurchasingSystem.hasDynamicPurchasingSystem
                        )
                    },
                    legalBasis = tender.legalBasis,
                    procurementMethod = tender.procurementMethod,
                    procurementMethodDetails = tender.procurementMethodDetails,
                    procurementMethodRationale = tender.procurementMethodRationale,
                    procurementMethodAdditionalInfo = tender.procurementMethodAdditionalInfo,
                    mainProcurementCategory = tender.mainProcurementCategory,
                    eligibilityCriteria = tender.eligibilityCriteria,
                    contractPeriod = tender.contractPeriod?.let { contractPeriod ->
                        CreateCnOnPnGpaResponse.Tender.ContractPeriod(
                            startDate = contractPeriod.startDate,
                            endDate = contractPeriod.endDate
                        )
                    },
                    procurementMethodModalities = tender.procurementMethodModalities,
                    electronicAuctions = tender.electronicAuctions?.let { electronicAuctions ->
                        CreateCnOnPnGpaResponse.Tender.ElectronicAuctions(
                            details = electronicAuctions.details.map { detail ->
                                CreateCnOnPnGpaResponse.Tender.ElectronicAuctions.Detail(
                                    id = detail.id,
                                    relatedLot = detail.relatedLot,
                                    electronicAuctionModalities = detail.electronicAuctionModalities.map { modality ->
                                        CreateCnOnPnGpaResponse.Tender.ElectronicAuctions.Detail.Modalities(
                                            eligibleMinimumDifference = modality.eligibleMinimumDifference.let { emd ->
                                                CreateCnOnPnGpaResponse.Tender.ElectronicAuctions.Detail.Modalities.EligibleMinimumDifference(
                                                    amount = emd.amount,
                                                    currency = emd.currency
                                                )
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    },
                    procuringEntity = tender.procuringEntity.let { procuringEntity ->
                        CreateCnOnPnGpaResponse.Tender.ProcuringEntity(
                            id = procuringEntity.id,
                            name = procuringEntity.name,
                            identifier = procuringEntity.identifier.let { identifier ->
                                CreateCnOnPnGpaResponse.Tender.ProcuringEntity.Identifier(
                                    scheme = identifier.scheme,
                                    id = identifier.id,
                                    legalName = identifier.legalName,
                                    uri = identifier.uri
                                )
                            },
                            additionalIdentifiers = procuringEntity.additionalIdentifiers?.map { additionalIdentifier ->
                                CreateCnOnPnGpaResponse.Tender.ProcuringEntity.AdditionalIdentifier(
                                    scheme = additionalIdentifier.scheme,
                                    id = additionalIdentifier.id,
                                    legalName = additionalIdentifier.legalName,
                                    uri = additionalIdentifier.uri
                                )
                            },
                            address = procuringEntity.address.let { address ->
                                CreateCnOnPnGpaResponse.Tender.ProcuringEntity.Address(
                                    streetAddress = address.streetAddress,
                                    postalCode = address.postalCode,
                                    addressDetails = address.addressDetails.let { addressDetails ->
                                        CreateCnOnPnGpaResponse.Tender.ProcuringEntity.Address.AddressDetails(
                                            country = addressDetails.country.let { country ->
                                                CreateCnOnPnGpaResponse.Tender.ProcuringEntity.Address.AddressDetails.Country(
                                                    scheme = country.scheme,
                                                    id = country.id,
                                                    description = country.description,
                                                    uri = country.uri
                                                )
                                            },
                                            region = addressDetails.region.let { region ->
                                                CreateCnOnPnGpaResponse.Tender.ProcuringEntity.Address.AddressDetails.Region(
                                                    scheme = region.scheme,
                                                    id = region.id,
                                                    description = region.description,
                                                    uri = region.uri
                                                )
                                            },
                                            locality = addressDetails.locality.let { locality ->
                                                CreateCnOnPnGpaResponse.Tender.ProcuringEntity.Address.AddressDetails.Locality(
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
                            contactPoint = procuringEntity.contactPoint.let { contactPoint ->
                                CreateCnOnPnGpaResponse.Tender.ProcuringEntity.ContactPoint(
                                    name = contactPoint.name,
                                    email = contactPoint.email,
                                    telephone = contactPoint.telephone,
                                    faxNumber = contactPoint.faxNumber,
                                    url = contactPoint.url
                                )
                            },
                            persones = procuringEntity.persones?.map { persone ->
                                CreateCnOnPnGpaResponse.Tender.ProcuringEntity.Persone(
                                    name = persone.name,
                                    title = persone.title,
                                    identifier = CreateCnOnPnGpaResponse.Tender.ProcuringEntity.Persone.Identifier(
                                        id = persone.identifier.id,
                                        scheme = persone.identifier.scheme,
                                        uri = persone.identifier.uri
                                    ),
                                    businessFunctions = persone.businessFunctions.map { businessFunction ->
                                        CreateCnOnPnGpaResponse.Tender.ProcuringEntity.Persone.BusinessFunction(
                                            id = businessFunction.id,
                                            type = businessFunction.type,
                                            jobTitle = businessFunction.jobTitle,
                                            period = CreateCnOnPnGpaResponse.Tender.ProcuringEntity.Persone.BusinessFunction.Period(
                                                startDate = businessFunction.period.startDate
                                            ),
                                            documents = businessFunction.documents?.map { document ->
                                                CreateCnOnPnGpaResponse.Tender.ProcuringEntity.Persone.BusinessFunction.Document(
                                                    id = document.id,
                                                    documentType = document.documentType,
                                                    title = document.title,
                                                    description = document.description
                                                )
                                            }

                                        )
                                    }
                                )
                            }
                        )
                    },
                    value = tender.value.let { value ->
                        CreateCnOnPnGpaResponse.Tender.Value(
                            amount = value.amount,
                            currency = value.currency
                        )
                    },
                    lotGroups = tender.lotGroups.map { lotGroup ->
                        CreateCnOnPnGpaResponse.Tender.LotGroup(
                            optionToCombine = lotGroup.optionToCombine
                        )
                    },
                    criteria = tender.criteria?.map { criteria ->
                        CreateCnOnPnGpaResponse.Tender.Criteria(
                            id = criteria.id,
                            title = criteria.title,
                            description = criteria.description,
                            requirementGroups = criteria.requirementGroups.map {
                                CreateCnOnPnGpaResponse.Tender.Criteria.RequirementGroup(
                                    id = it.id,
                                    description = it.description,
                                    requirements = it.requirements.map { requirement ->
                                        Requirement(
                                            id = requirement.id,
                                            description = requirement.description,
                                            title = requirement.title,
                                            period = requirement.period?.let { period ->
                                                Period(
                                                    startDate = period.startDate,
                                                    endDate = period.endDate
                                                )
                                            },
                                            dataType = requirement.dataType,
                                            value = requirement.value
                                        )
                                    }
                                )
                            },
                            relatesTo = criteria.relatesTo,
                            relatedItem = criteria.relatedItem
                        )
                    },
                    conversions = tender.conversions?.map { conversion ->
                        CreateCnOnPnGpaResponse.Tender.Conversion(
                            id = conversion.id,
                            relatedItem = conversion.relatedItem,
                            relatesTo = conversion.relatesTo,
                            rationale = conversion.rationale,
                            description = conversion.description,
                            coefficients = conversion.coefficients.map { coefficient ->
                                CreateCnOnPnGpaResponse.Tender.Conversion.Coefficient(
                                    id = coefficient.id,
                                    value = coefficient.value,
                                    coefficient = coefficient.coefficient
                                )
                            }
                        )
                    },
                    lots = tender.lots.map { lot ->
                        CreateCnOnPnGpaResponse.Tender.Lot(
                            id = lot.id,
                            internalId = lot.internalId,
                            title = lot.title,
                            description = lot.description,
                            status = lot.status,
                            statusDetails = lot.statusDetails,
                            value = lot.value.let { value ->
                                CreateCnOnPnGpaResponse.Tender.Lot.Value(
                                    amount = value.amount,
                                    currency = value.currency
                                )
                            },
                            options = lot.options.map { option ->
                                CreateCnOnPnGpaResponse.Tender.Lot.Option(
                                    hasOptions = option.hasOptions
                                )
                            },
                            variants = lot.variants.map { variant ->
                                CreateCnOnPnGpaResponse.Tender.Lot.Variant(
                                    hasVariants = variant.hasVariants
                                )
                            },
                            renewals = lot.renewals.map { renewal ->
                                CreateCnOnPnGpaResponse.Tender.Lot.Renewal(
                                    hasRenewals = renewal.hasRenewals
                                )
                            },
                            recurrentProcurement = lot.recurrentProcurement.map { recurrentProcurement ->
                                CreateCnOnPnGpaResponse.Tender.Lot.RecurrentProcurement(
                                    isRecurrent = recurrentProcurement.isRecurrent
                                )
                            },
                            contractPeriod = lot.contractPeriod.let { contractPeriod ->
                                CreateCnOnPnGpaResponse.Tender.Lot.ContractPeriod(
                                    startDate = contractPeriod.startDate,
                                    endDate = contractPeriod.endDate
                                )
                            },
                            placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                                CreateCnOnPnGpaResponse.Tender.Lot.PlaceOfPerformance(
                                    description = placeOfPerformance.description,
                                    address = placeOfPerformance.address.let { address ->
                                        CreateCnOnPnGpaResponse.Tender.Lot.PlaceOfPerformance.Address(
                                            streetAddress = address.streetAddress,
                                            postalCode = address.postalCode,
                                            addressDetails = address.addressDetails.let { addressDetails ->
                                                CreateCnOnPnGpaResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                                    country = addressDetails.country.let { country ->
                                                        CreateCnOnPnGpaResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                            scheme = country.scheme,
                                                            id = country.id,
                                                            description = country.description,
                                                            uri = country.uri
                                                        )
                                                    },
                                                    region = addressDetails.region.let { region ->
                                                        CreateCnOnPnGpaResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                            scheme = region.scheme,
                                                            id = region.id,
                                                            description = region.description,
                                                            uri = region.uri
                                                        )
                                                    },
                                                    locality = addressDetails.locality.let { locality ->
                                                        CreateCnOnPnGpaResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                                                            scheme = locality.scheme,
                                                            id = locality.id,
                                                            description = locality.description,
                                                            uri = locality.uri
                                                        )
                                                    }

                                                )
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    },
                    items = tender.items.map { item ->
                        CreateCnOnPnGpaResponse.Tender.Item(
                            id = item.id,
                            internalId = item.internalId,
                            classification = item.classification.let { classification ->
                                CreateCnOnPnGpaResponse.Tender.Item.Classification(
                                    scheme = classification.scheme,
                                    id = classification.id,
                                    description = classification.description
                                )
                            },
                            additionalClassifications = item.additionalClassifications?.map { additionalClassification ->
                                CreateCnOnPnGpaResponse.Tender.Item.AdditionalClassification(
                                    scheme = additionalClassification.scheme,
                                    id = additionalClassification.id,
                                    description = additionalClassification.description
                                )
                            },
                            quantity = item.quantity,
                            unit = item.unit.let { unit ->
                                CreateCnOnPnGpaResponse.Tender.Item.Unit(
                                    id = unit.id,
                                    name = unit.name
                                )
                            },
                            description = item.description,
                            relatedLot = item.relatedLot
                        )
                    },
                    awardCriteria = tender.awardCriteria,
                    awardCriteriaDetails = tender.awardCriteriaDetails,
                    submissionMethod = tender.submissionMethod,
                    submissionMethodRationale = tender.submissionMethodRationale,
                    submissionMethodDetails = tender.submissionMethodDetails,
                    documents = tender.documents.map { document ->
                        CreateCnOnPnGpaResponse.Tender.Document(
                            documentType = document.documentType,
                            id = document.id,
                            title = document.title,
                            description = document.description,
                            relatedLots = document.relatedLots
                        )
                    },
                    secondStage = tender.secondStage
                        ?.let { secondStage ->
                            CreateCnOnPnGpaResponse.Tender.SecondStage(
                                minimumCandidates = secondStage.minimumCandidates,
                                maximumCandidates = secondStage.maximumCandidates
                            )
                        }
                )
            }
        )
    }

    private fun planning(pnEntity: PNEntity): CNEntity.Planning {
        return CNEntity.Planning(
            rationale = pnEntity.planning.rationale,
            budget = CNEntity.Planning.Budget(
                description = pnEntity.planning.budget.description,
                amount = pnEntity.planning.budget.amount.let {
                    CNEntity.Planning.Budget.Amount(
                        amount = it.amount,
                        currency = it.currency
                    )
                },
                isEuropeanUnionFunded = pnEntity.planning.budget.isEuropeanUnionFunded,
                budgetBreakdowns = pnEntity.planning.budget.budgetBreakdowns.map { budgetBreakdown ->
                    CNEntity.Planning.Budget.BudgetBreakdown(
                        id = budgetBreakdown.id,
                        description = budgetBreakdown.description,
                        amount = budgetBreakdown.amount.let {
                            CNEntity.Planning.Budget.BudgetBreakdown.Amount(
                                amount = it.amount,
                                currency = it.currency
                            )
                        },
                        period = budgetBreakdown.period.let {
                            CNEntity.Planning.Budget.BudgetBreakdown.Period(
                                startDate = it.startDate,
                                endDate = it.endDate
                            )
                        },
                        sourceParty = budgetBreakdown.sourceParty.let {
                            CNEntity.Planning.Budget.BudgetBreakdown.SourceParty(
                                name = it.name,
                                id = it.id
                            )
                        },
                        europeanUnionFunding = budgetBreakdown.europeanUnionFunding?.let {
                            CNEntity.Planning.Budget.BudgetBreakdown.EuropeanUnionFunding(
                                projectIdentifier = it.projectIdentifier,
                                projectName = it.projectName,
                                uri = it.uri
                            )
                        }
                    )
                }
            )
        )
    }

    /**
     * VR-3.8.3(CN on PN) Documents ->  VR-3.6.1(CN)
     *                              ->  VR-3.7.3(CN)
     *
     * Checks the uniqueness of all documents.ID from Request;
     * IF there is NO repeated value in list of documents.ID values from Request, validation is successful;
     * ELSE eAccess throws Exception: "Invalid documents IDs";
     *
     * VR-3.6.1(CN)
     * eAccess валидирует наличие секции Documents (tender/Documents) в секции Tender запроса.
     * Производится проверка, что "documentType" (tender.Documents.documentType) каждого объекта секции Documents
     * запроса равен одному из значений из списка
     *
     * VR-3.7.3(CN)
     * eAccess проверяет, что все Document.ID документов сохраненной версии тендера имеют соответствие
     * в массиве Documents из запроса.
     */
    private fun checkDocuments(
        documentsFromRequest: List<CheckCnOnPnGpaRequest.Tender.Document>,
        documentsFromPN: List<PNEntity.Tender.Document>?
    ) {
        val uniqueIdsDocumentsFromRequest: Set<String> = documentsFromRequest.toSetBy { it.id }
        if (uniqueIdsDocumentsFromRequest.size != documentsFromRequest.size)
            throw ErrorException(INVALID_DOCS_ID)

        documentsFromPN?.toSetBy { it.id }
            ?.forEach { id ->
                if (id !in uniqueIdsDocumentsFromRequest) {
                    throw ErrorException(
                        error = INVALID_DOCS_ID,
                        message = "The request is missing a document with id '$id'"
                    )
                }
            }
    }

    /**
     * VR-1.0.1.11.1
     *
     * eAccess checks availability at least one value: minimumCandidates or maximumCandidates from the context of Request:
     * a. IF [there is at least one value minimumCandidates or maximumCandidates in Request] then: validation is successful;
     * b. } else {  eAccess throws Exception: "At least one value should be: minimumCandidates or maximumCandidates";
     */
    private fun CheckCnOnPnGpaRequest.Tender.SecondStage.validateCompleteness() {
        if (this.minimumCandidates == null && this.maximumCandidates == null)
            throw ErrorException(
                error = ErrorType.INVALID_SECOND_STAGE,
                message = "Path: tender.secondStage. At least one value should be: minimumCandidates or maximumCandidates"
            )
    }

    /**
     * VR-1.0.1.11.2
     *
     * eAccess checks minimumCandidates values from the context of Request:
     * a. IF [secondStage.minimumCandidates value from Request > (more) 0] then: validation is successful;
     * b. else eAccess throws Exception: "minimumCandidates value should not be zero";
     */
    private fun validateMinimumCandidates(amount: Int) {
        if (amount <= 0)
            throw ErrorException(
                error = ErrorType.INVALID_SECOND_STAGE,
                message = "Path: tender.secondStage.minimumCandidates. MinimumCandidates value should not be less or equals than zero"
            )
    }

    /**
     * VR-1.0.1.11.3
     *
     * eAccess checks maximumCandidates values from the context of Request:
     * a. IF [secondStage.maximumCandidates value from Request > (more) 0] then: validation is successful;
     * b. else eAccess throws Exception: "MaximumCandidates value should not be zero";
     */
    private fun validateMaximumCandidates(amount: Int) {
        if (amount <= 0)
            throw ErrorException(
                error = ErrorType.INVALID_SECOND_STAGE,
                message = "Path: tender.secondStage.maximumCandidates. MaximumCandidates value should not be less or equals than zero"
            )
    }

    /**
     * VR-1.0.1.11.4
     *
     * eAccess compares secondStage.minimumCandidates and maximumCandidates from the context of Request:
     * a. IF [secondStage.minimumCandidates < (less) maximumCandidates from Request]  then: validation is successful;
     * b. else eAccess throws Exception: "MaximumCandidates value should be more or equal MinimumCandidates ";
     */
    private fun validateCandidatesRange(min: Int, max: Int) {
        if (min >= max)
            throw ErrorException(
                error = ErrorType.INVALID_SECOND_STAGE,
                message = "Path: tender.secondStage.maximumCandidates. MaximumCandidates value should be more than MinimumCandidates "
            )
    }


    /**
     * VR-1.0.1.10.1
     *
     * eAccess compares procuringEntity.ID related to saved PN || CN from DB and procuringEntity.ID from Request:
     * IF [procuringEntity.ID value in DB ==  (equal to) procuringEntity.ID from Request] then: validation is successful; }
     * else {  eAccess throws Exception: "Invalid identifier of procuring entity";}
     *
     */
    private fun checkProcuringEntityIdentifier(
        procuringEntityRequest: CheckCnOnPnGpaRequest.Tender.ProcuringEntity,
        procuringEntityDB: PNEntity.Tender.ProcuringEntity
    ) {
        if (procuringEntityDB.id != procuringEntityRequest.id) throw ErrorException(
            error = INVALID_PROCURING_ENTITY,
            message = "Invalid identifier of procuring entity. " +
                "Request.procuringEntity.id (=${procuringEntityRequest.id})  != " +
                "DB.procuringEntity.id (=${procuringEntityDB.id}). "
        )
    }

    /**
     * VR-1.0.1.10.2
     *
     * eAccess checks the avaliability of at least one procuringEntity.Persones object in array  from Request:
     * IF [there is at least one Persones object in Request] then validation is successful; }
     * else { eAccess throws Exception: "At least one Person should be added"; }
     *
     *
     * VR-1.0.1.10.3
     * eAccess checks the uniqueness of all Persones.identifier.ID values from every object of Persones array of Request:
     * IF [there is NO repeated values of identifier.ID in Request] then validation is successful; }
     * else { eAccess throws Exception: "Persones objects should be unique in Request"; }
     *
     *
     * VR-1.0.1.10.6
     *
     * eAccess checks persones.businessFunctions.type values in all businessFuctions object from Request;
     * IF businessFunctions.type == oneOf procuringEntityBusinessFuncTypeEnum value (link), validation is successful;}
     * else {  eAccess throws Exception: "Invalid business functions type";
     */
    private fun checkProcuringEntityPersones(
        procuringEntityRequest: CheckCnOnPnGpaRequest.Tender.ProcuringEntity
    ) {

        procuringEntityRequest.persones
            ?.also {
                if (it.isEmpty()) throw ErrorException(
                    error = INVALID_PROCURING_ENTITY,
                    message = "At least one Person should be added. "
                )

                val personesIdentifier = it.map { it.identifier }
                val personesIdentifierUnique = personesIdentifier.toSet()
                if (personesIdentifier.size != personesIdentifierUnique.size) throw ErrorException(
                    error = INVALID_PROCURING_ENTITY,
                    message = "Persones objects should be unique in Request. "
                )
            }
    }

    /**
     * VR-1.0.1.10.4
     *
     * eAccess checks the avaliability of at least one persones.businessFunctions object in businessFunctions array  from Request:
     * IF [there is at least one businessFunctions object in appropriate Persones object] then validation is successful; }
     * else { eAccess throws Exception: "At least one businessFunctions detaluzation should be added"; }
     *
     *
     * VR-1.0.1.10.5
     *
     * eAccess checks the uniqueness of all Persones.businessFunctions.ID values from every object of businessFunctions array of Request:
     * IF [there is NO repeated values of businessFunctions.ID] then validation is successful; }
     * else { eAccess throws Exception: "businessFunctions objects should be unique in every Person from Request"; }
     *
     */
    private fun checkPersonesBusinessFunctions(
        procuringEntityRequest: CheckCnOnPnGpaRequest.Tender.ProcuringEntity
    ) {

        procuringEntityRequest.persones
            ?.map { it.businessFunctions }
            ?.forEach { businessfunctions ->
                if (businessfunctions.isEmpty()) throw ErrorException(
                    error = INVALID_PROCURING_ENTITY,
                    message = "At least one businessFunctions detalization should be added. "
                )
                if (businessfunctions.toSetBy { it.id }.size != businessfunctions.size) throw ErrorException(
                    error = INVALID_PROCURING_ENTITY,
                    message = "businessFunctions objects should be unique in every Person from Request. "
                )
            }

        procuringEntityRequest.persones
            ?.flatMap { it.businessFunctions }
            ?.forEach {
                when (it.type) {
                    BusinessFunctionType.CHAIRMAN,
                    BusinessFunctionType.PROCURMENT_OFFICER,
                    BusinessFunctionType.CONTACT_POINT,
                    BusinessFunctionType.TECHNICAL_EVALUATOR,
                    BusinessFunctionType.TECHNICAL_OPENER,
                    BusinessFunctionType.PRICE_OPENER,
                    BusinessFunctionType.PRICE_EVALUATOR -> Unit

                    BusinessFunctionType.AUTHORITY       -> throw ErrorException(
                        error = ErrorType.INVALID_BUSINESS_FUNCTION,
                        message = "Type '${BusinessFunctionType.AUTHORITY.key}' was deprecated. Use '${BusinessFunctionType.CHAIRMAN}' instead of it"
                    )
                }
            }
    }

    /**
     * VR-1.0.1.10.7
     *
     * eAccess compares businessFunctions.period.startDate and startDate from the context of Request:
     * IF [businessFunctions.period.startDate <= (less || equal to) startDate from Request] then: validation is successful; }
     * else {  eAccess throws Exception: "Invalid period in bussiness function specification"; }
     *
     */
    private fun checkBusinessFunctionPeriod(
        procuringEntityRequest: CheckCnOnPnGpaRequest.Tender.ProcuringEntity,
        context: CheckCnOnPnGpaContext
    ) {
        fun dateError(): Nothing = throw ErrorException(
            error = INVALID_PROCURING_ENTITY,
            message = "Invalid period in bussiness function specification. "
        )

        procuringEntityRequest.persones
            ?.flatMap { it.businessFunctions }
            ?.forEach { if (it.period.startDate > context.startDate) dateError() }
    }

    /**
     * VR-1.0.1.2.1
     *
     * eAccess checks the uniqueness of all documents.ID from Request;
     * if there is NO repeated value in list of documents.ID values from Request, validation is successful;
     * else {  eAccess throws Exception: "Invalid documents IDs";
     *
     *
     * VR-1.0.1.2.7
     *
     * eAccess checks the avaliability of at least one tender.documents || bussinessFunctions.documents object in array from Request:
     * IF [there is at least one Document object in Request] then validation is successful; }
     * else { eAccess throws Exception: "At least one document should be added"; }
     *
     *
     * VR-1.0.1.2.8
     *
     * eAccess checks documents.documentType values in all Documents object from Request;
     * IF document.documentType == oneOf bussinesFunctionsDocumentTupeEnum value (link), validation is successful; }
     * else {  eAccess throws Exception: "Invalid document type"; }
     */
    private fun checkBusinessFunctionDocuments(
        procuringEntityRequest: CheckCnOnPnGpaRequest.Tender.ProcuringEntity
    ) {

        procuringEntityRequest.persones
            ?.flatMap { it.businessFunctions }
            ?.forEach { businessFunction ->
                businessFunction.documents?.let { documents ->
                    if (documents.isEmpty()) throw ErrorException(
                        error = ErrorType.EMPTY_DOCS,
                        message = "At least one document should be added to businessFunction documents. "
                    )

                    val actualIds = documents.map { it.id }
                    val uniqueIds = actualIds.toSet()

                    if (actualIds.size != uniqueIds.size) throw ErrorException(
                        error = INVALID_DOCS_ID,
                        message = "Invalid documents IDs. Ids not unique [${actualIds}]. "
                    )

                    documents.forEach {
                        when (it.documentType) {
                            BusinessFunctionDocumentType.REGULATORY_DOCUMENT -> Unit
                        }
                    }
                }
            }
    }

    private fun checkTenderDocumentsNotEmpty(
        tender: CheckCnOnPnGpaRequest.Tender
    ) {
        if (tender.documents.isEmpty()) throw ErrorException(
            error = ErrorType.EMPTY_DOCS,
            message = "At least one document should be added to tenders documents. "
        )
    }

    /**
     * VR-3.8.4(CN on PN) "Value" (tender)
     *
     * eAccess проверяет, что "Value" (tender.value.amount), полученное по BR-3.8.14(CN),
     * меньше / ровно значения поля «Budget Value» (budget.amount.amount) from DB.
     */
    private fun checkTenderValue(tenderValueAmount: BigDecimal, budget: PNEntity.Planning.Budget) {
        if (tenderValueAmount > budget.amount.amount)
            throw ErrorException(
                error = INVALID_TENDER_AMOUNT,
                message = "The amount of the tender [$tenderValueAmount] more that the amount of the budget [${budget.amount.amount}]."
            )
    }

    /**
     * VR-3.8.5(CN on PN)  "Currency" (lot)
     *
     * eAccess проверяет, что значение "Currency" (tender.lot.value.currency)
     * from Request == "Currency" (budget.amount.currency) from saved PNEntity.
     */
    private fun checkCurrencyInLotsFromRequest(
        lotsFromRequest: List<CheckCnOnPnGpaRequest.Tender.Lot>,
        budgetFromPN: PNEntity.Planning.Budget
    ) {
        lotsFromRequest.forEach { lot ->
            if (lot.value.currency != budgetFromPN.amount.currency)
                throw ErrorException(
                    error = INVALID_LOT_CURRENCY,
                    message = "Lot with id: '${lot.id}' contains invalid currency (lot currency: '${lot.value.currency}', budget amount currency: '${budgetFromPN.amount.currency}')"
                )
        }
    }

    /**
     * VR-3.8.6(CN on PN)  "Contract Period"(Tender) -> VR-3.6.10(CN)
     *
     * VR-3.6.10(CN)	"Contract Period" (Tender)
     * eAccess проверяет, что значение ContractPeriod.StartDate (tender/contractPeriod/startDate),
     * определенное по правилу BR-3.6.31, -  меньше или равно каждому значению budgetBreakdown.period.endDate
     * AND ContractPeriod.EndDate (tender/contractPeriod/endDate) - больше или равен каждому значению budgetBreakdown.period.startDate
     * добавляемых FS:
     *
     * tender.contractPeriod.startDate  <= planning.budget.budgetBreakdown.period.endDate
     * tender.contractPeriod.endDate  >= planning.budget.budgetBreakdown.period.startDate
     *
     *Пример:
     * tender.contractPeriod.startDate = 10.06.2017 AND tender.contractPeriod.endDate = 30.10.2017
     * Period of budgetBreakdown1 [01.06.2017 - 10.08.2017] - budgetBreakdown is OK
     * Period of budgetBreakdown2 [31.10.2017 - 30.11.2017] - budgetBreakdown isn't OK
     * Period of budgetBreakdown3 [01.01.2017 - 09.06.2017] - budgetBreakdown isn't OK
     * Period of budgetBreakdown4 [03.03.2017 - 10.06.2017] - budgetBreakdown is OK
     *
     * BR-3.6.31(CN)	"Contract Period" (Tender)
     * eAccess определяет "Contract Period: Start Date" (tender.contractPeriod.startDate) == наиболее раннему значению
     * из полей "Contract Period: Start Date" (tender.lots.contractPeriod.startDate)
     * всех добавленных объектов секции Lots запроса.
     *
     * eAccess определяет "Contract Period: End Date" (tender/contractPeriod/endDate) == наиболее позднему значению
     * из полей "Contract Period: End Date" (tender/lots/contractPeriod/endDate)
     * всех добавленных объектов секции Lots запроса.
     */
    private fun checkContractPeriodInTender(
        lotsFromRequest: List<CheckCnOnPnGpaRequest.Tender.Lot>,
        budgetBreakdownsFromPN: List<PNEntity.Planning.Budget.BudgetBreakdown>
    ) {
        val tenderContractPeriod = calculationTenderContractPeriod(lotsFromRequest)
        checkContractPeriodInTender(tenderContractPeriod, budgetBreakdownsFromPN)
    }

    private fun checkContractPeriodInTender(
        tenderContractPeriod: CNEntity.Tender.ContractPeriod,
        budgetBreakdownsFromPN: List<PNEntity.Planning.Budget.BudgetBreakdown>
    ) {
        budgetBreakdownsFromPN.forEach { budgetBreakdown ->
            if (tenderContractPeriod.startDate > budgetBreakdown.period.endDate)
                throw ErrorException(
                    error = INVALID_LOT_CONTRACT_PERIOD,
                    message = "The start date of the tender contract period [${tenderContractPeriod.startDate}] after than the end date of the budget breakdown period [${budgetBreakdown.period.endDate}]"
                )
            if (tenderContractPeriod.endDate < budgetBreakdown.period.startDate)
                throw ErrorException(
                    error = INVALID_LOT_CONTRACT_PERIOD,
                    message = "The end date of the tender contract period [${tenderContractPeriod.endDate}] before than the start date of the budget breakdown period [${budgetBreakdown.period.startDate}]"
                )
        }
    }

    /**
     * VR-3.8.7(CN on PN)  "Related Lots"(documents) -> VR-3.6.12(CN)
     *
     * VR-3.6.12 "Related Lots" (documents)
     * Access проверяет, что значения указанные в поле relatedLots (document.relatedLots) каждого объекта
     * секции Documents имеют соответствие в списке значений tender.lots.id.
     */
    private fun checkRelatedLotsInDocumentsFromRequestWhenPNWithoutItems(
        lotsIdsFromRequest: Set<String>,
        documentsFromRequest: List<CheckCnOnPnGpaRequest.Tender.Document>
    ) {
        documentsFromRequest.forEach { document ->
            document.relatedLots?.forEach { relatedLot ->
                if (relatedLot !in lotsIdsFromRequest)
                    throw ErrorException(INVALID_DOCS_RELATED_LOTS)
            }
        }
    }

    /**
     *  VR-1.0.1.4.3
     *
     * IF (pmd == "GPA") { eAccess checks lot.contractPeriod.startDate in every Lot object from Request:
     *      IF value of lot.contractPeriod.startDate from Request > (later than) value of preQualification.period.endDate
     *      from Request, validation is successful;
     *      else { eAccess throws Exception: "Invalid date-time values in lot contract period";
     */
    private fun checkContractPeriodInLotsWhenPNWithoutItemsFromRequest(tenderFromRequest: CheckCnOnPnGpaRequest) {
        val preQualificationPeriodEndDate = tenderFromRequest.preQualification.period.endDate
        tenderFromRequest.tender.lots.forEach { lot ->
            checkRangeContractPeriodInLotFromRequest(lot)
            if (lot.contractPeriod.startDate <= preQualificationPeriodEndDate)
                throw ErrorException(
                    error = INVALID_LOT_CONTRACT_PERIOD,
                    message = "The start date [${lot.contractPeriod.startDate}] of the contract period of the lot [${lot.id}] less or equals that the end date of the tender period [$preQualificationPeriodEndDate]."
                )
        }
    }

    private fun checkRangeContractPeriodInLotFromRequest(lot: CheckCnOnPnGpaRequest.Tender.Lot) {
        if (lot.contractPeriod.startDate >= lot.contractPeriod.endDate)
            throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
    }

    /**
     * VR-3.8.9(CN on PN) "Quantity" (item) -> VR-3.6.11(CN)
     *
     * VR-3.6.11 "Quantity" (item)
     * eAccess проверяет, что значению "Quantity" (tender/items/quantity) каждого объекта секции Items больше нуля.
     */
    private fun checkQuantityInItems(itemsFromRequest: List<CheckCnOnPnGpaRequest.Tender.Item>) {
        itemsFromRequest.forEach { item ->
            if (item.quantity <= BigDecimal.ZERO)
                throw ErrorException(ErrorType.INVALID_ITEMS_QUANTITY)
        }
    }

    /**
     * VR-3.8.10(CN on PN) Lots (tender.lots) -> VR-3.6.9(CN)
     *
     * VR-3.6.9	Lots
     * eAccess performs next steps:
     * Checks the quantity of Lot object in Request:
     *      IF quantity of Lot object in Request > 0, validation is successful;
     *      ELSE eAccess throws Exception;
     *
     * Analyzes Lot.ID from Request:
     *      IF all lot.ID from Request are presented in list of values item.relatedLot from Request (at least once),
     *      validation is successful;
     *      ELSE eAccess throws Exception;
     */
    private fun checkLotIdsAsRelatedLotInItems(
        lotsIdsFromRequest: Set<String>,
        itemsFromRequest: List<CheckCnOnPnGpaRequest.Tender.Item>
    ) {
        if (lotsIdsFromRequest.isEmpty())
            throw ErrorException(ErrorType.EMPTY_LOTS)

        val itemsRelatedLots: Set<String> = itemsFromRequest.toSetBy { it.relatedLot }
        lotsIdsFromRequest.forEach { lotId ->
            if (lotId !in itemsRelatedLots)
                throw ErrorException(
                    error = ErrorType.LOT_ID_NOT_MATCH_TO_RELATED_LOT_IN_ITEMS,
                    message = "Lot with id='${lotId}' not matches any relatedLots in items"
                )
        }
    }

    /**
     * VR-3.8.11(CN on PN) Items (tender.Items) -> VR-3.6.8(CN)
     *
     * VR-3.6.8 (CN)	Items
     * eAccess проверяет, что используется также Lots section (tender/lots).
     *
     * eAccess проверяет, что значению "Related Lot" (tender/items/relatedLot)
     * каждого объекта секции Items соответствует объект секции Lots по полю "Id" (tender/lots/id).
     */
    private fun checkRelatedLotInItemsFromRequest(
        lotsIdsFromRequest: Set<String>,
        itemsFromRequest: List<CheckCnOnPnGpaRequest.Tender.Item>
    ) {
        itemsFromRequest.forEach { item ->
            val relatedLot = item.relatedLot
            if (relatedLot !in lotsIdsFromRequest)
                throw ErrorException(INVALID_ITEMS_RELATED_LOTS)
        }
    }

    /**
     * VR-3.8.12(CN on PN) Lot.ID -> VR-3.1.14(CN)
     *
     * VR-3.1.14	Lot.ID
     * eAccess analyzes Lot.ID from Request:
     * IF every lot.ID from Request is included once in list from Request, validation is successful;
     * ELSE eAccess throws Exception;
     */
    private fun checkLotIdFromRequest(lotsFromRequest: List<CheckCnOnPnGpaRequest.Tender.Lot>) {
        val idsAreUniques = lotsFromRequest.uniqueBy { it.id }
        if (idsAreUniques.not())
            throw throw ErrorException(LOT_ID_DUPLICATED)
    }

    /**
     * VR-3.8.13(CN on PN) Item.ID -> VR-3.1.15(CN)
     *
     * VR-3.1.15	Item.ID
     * eAccess analyzes item.ID from Request:
     * IF every item.ID from Request is included once in list from Request, validation is successful;
     * ELSE eAccess throws Exception;
     */
    private fun checkItemIdFromRequest(itemsFromRequest: List<CheckCnOnPnGpaRequest.Tender.Item>) {
        val idsAreUniques = itemsFromRequest.uniqueBy { it.id }
        if (idsAreUniques.not())
            throw throw ErrorException(ITEM_ID_IS_DUPLICATED)
    }

    /**
     * VR-3.8.16(CN on PN) "Contract Period" (Lot)
     *
     * IF (pmd == "GPA")  eAccess checks lot.contractPeriod.startDate in every Lot object from DB:
     *   IF value of lot.contractPeriod.startDate from DB > (later than) value of preQualification.period.endDate
     *   from Request, validation is successful;
     *   else eAccess throws Exception: "Invalid date-time values in lot contract period";
     */
    private fun checkContractPeriodInLotsFromRequestWhenPNWithItems(
        preQualificationPeriodEndDate: LocalDateTime,
        lotsFromPN: List<PNEntity.Tender.Lot>
    ) {
        lotsFromPN.forEach { lot ->
            if (lot.contractPeriod.startDate <= preQualificationPeriodEndDate)
                throw ErrorException(
                    error = INVALID_LOT_CONTRACT_PERIOD,
                    message = "The start date [${lot.contractPeriod.startDate}] of the contract period of the lot [${lot.id}] less or eq that the tender period end date [$preQualificationPeriodEndDate]. "
                )
        }
    }

    /**
     * VR-3.8.17(CN on PN)  "Related Lots"(documents) -> VR-3.7.13(Update CNEntity)
     *
     * VR-3.6.12 "Related Lots" (documents)
     * Access проверяет, что значения указанные в поле relatedLots (document.relatedLots) каждого объекта
     * секции Documents имеют соответствие в списке значений tender.lots.id.
     */
    private fun checkRelatedLotsInDocumentsFromRequestWhenPNWithItems(
        lotsIdsFromPN: Set<String>,
        documentsFromRequest: List<CheckCnOnPnGpaRequest.Tender.Document>
    ) {
        documentsFromRequest.forEach { document ->
            document.relatedLots?.forEach { relatedLot ->
                if (relatedLot !in lotsIdsFromPN)
                    throw ErrorException(
                        error = INVALID_DOCS_RELATED_LOTS,
                        message = "The document from request with id '${document.id}' contains invalid related lot '$relatedLot'. Valid lot ids: $lotsIdsFromPN."
                    )
            }
        }
    }

    /**
     * VR-1.0.1.7.7
     */
    private fun checkAuctionsAreRequired(
        context: CheckCnOnPnGpaContext,
        data: CheckCnOnPnGpaRequest,
        mainProcurementCategory: MainProcurementCategory
    ) {
        val isAuctionRequired = rulesService.isAuctionRequired(
            context.country,
            context.pmd,
            mainProcurementCategory
        )

        if (isAuctionRequired) {
            val procurementMethodModalities = data.tender.procurementMethodModalities
            if (procurementMethodModalities == null || procurementMethodModalities.isEmpty())
                throw ErrorException(INVALID_PMM)

            val electronicAuctions = data.tender.electronicAuctions
            if (electronicAuctions == null || electronicAuctions.details.isEmpty())
                throw ErrorException(ErrorType.INVALID_AUCTION_IS_EMPTY)
        }
    }

    /**
     * VR-3.8.18 Status (tender)
     *
     * eAccess analyzes tender.status value from DB:
     * IF tender.status in DB == "planning", validation is successful;
     * ELSE (tender.status in DB != "planning") eAccess throws Exception: "Planning Notice can not be used";
     */
    private fun checkTenderStatus(pnEntity: PNEntity) {
        if (pnEntity.tender.status == TenderStatus.UNSUCCESSFUL)
            throw ErrorException(
                error = ErrorType.TENDER_IN_UNSUCCESSFUL_STATUS,
                message = "The tender is unsuccessful."
            )
    }

    /**
     * BR-3.8.14(CN on PN) -> BR-3.6.30(CN)
     *
     * eAccess add object "Value":
     *      "Amount" (tender.value.amount) is obtained by summation of values from "Amount" (tender.lot.value.amount)
     *      of all lot objects from Request.
     *      eAccess sets "Currency" (tender.value.currency) == "Currency" (tender.lot.value.currency) from Request.
     */
    private fun calculateTenderValueFromLots(lotsFromRequest: List<CheckCnOnPnGpaRequest.Tender.Lot>): CNEntity.Tender.Value {
        val currency = lotsFromRequest.elementAt(0).value.currency
        val totalAmount = lotsFromRequest.fold(BigDecimal.ZERO) { acc, lot ->
            acc.plus(lot.value.amount)
        }.setScale(2, RoundingMode.HALF_UP)
        return CNEntity.Tender.Value(totalAmount, currency)
    }

    /**
     * BR-3.8.14(CN on PN) -> BR-3.6.30(CN)
     *
     * eAccess add object "Value":
     *      "Amount" (tender.value.amount) is obtained by summation of values from "Amount" (tender.lot.value.amount)
     *      of all lot objects from Request.
     *      eAccess sets "Currency" (tender.value.currency) == "Currency" (tender.lot.value.currency) from Request.
     */
    private fun calculateTenderValueFromLotsGpaCheck(lotsFromRequest: List<CreateCnOnPnGpaRequest.Tender.Lot>): CNEntity.Tender.Value {
        val currency = lotsFromRequest.elementAt(0).value.currency
        val totalAmount = lotsFromRequest.fold(BigDecimal.ZERO) { acc, lot ->
            acc.plus(lot.value.amount)
        }.setScale(2, RoundingMode.HALF_UP)
        return CNEntity.Tender.Value(totalAmount, currency)
    }

    private fun calculationTenderContractPeriod(lots: List<CheckCnOnPnGpaRequest.Tender.Lot>): CNEntity.Tender.ContractPeriod {
        val contractPeriodSet = lots.asSequence().map { it.contractPeriod }.toSet()
        val startDate = contractPeriodSet.minBy { it.startDate }!!.startDate
        val endDate = contractPeriodSet.maxBy { it.endDate }!!.endDate
        return CNEntity.Tender.ContractPeriod(startDate, endDate)
    }

    private fun calculationTenderContractPeriodGpaCreate(lots: List<CreateCnOnPnGpaRequest.Tender.Lot>): CNEntity.Tender.ContractPeriod {
        val contractPeriodSet = lots.asSequence().map { it.contractPeriod }.toSet()
        val startDate = contractPeriodSet.minBy { it.startDate }!!.startDate
        val endDate = contractPeriodSet.maxBy { it.endDate }!!.endDate
        return CNEntity.Tender.ContractPeriod(startDate, endDate)
    }

}

