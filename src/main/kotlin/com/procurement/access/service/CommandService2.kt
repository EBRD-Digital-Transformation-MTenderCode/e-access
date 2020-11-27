package com.procurement.access.service

import com.procurement.access.domain.fail.error.BadRequest
import com.procurement.access.infrastructure.api.v2.ApiResponseV2
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.calculate.value.CalculateAPValueHandler
import com.procurement.access.infrastructure.handler.check.accesstotender.CheckAccessToTenderHandler
import com.procurement.access.infrastructure.handler.check.auction.CheckExistenceSignAuctionHandler
import com.procurement.access.infrastructure.handler.check.currency.CheckEqualityCurrenciesHandler
import com.procurement.access.infrastructure.handler.check.fa.CheckExistenceFAHandler
import com.procurement.access.infrastructure.handler.check.persons.CheckPersonsStructureHandler
import com.procurement.access.infrastructure.handler.check.relation.CheckRelationHandler
import com.procurement.access.infrastructure.handler.check.tenderstate.CheckTenderStateHandler
import com.procurement.access.infrastructure.handler.create.CreateCriteriaForProcuringEntityHandler
import com.procurement.access.infrastructure.handler.create.relation.CreateRelationToOtherProcessHandler
import com.procurement.access.infrastructure.handler.find.auction.FindAuctionsHandler
import com.procurement.access.infrastructure.handler.find.criteria.FindCriteriaHandler
import com.procurement.access.infrastructure.handler.get.criteria.GetQualificationCriteriaAndMethodHandler
import com.procurement.access.infrastructure.handler.get.currency.GetCurrencyHandler
import com.procurement.access.infrastructure.handler.get.lotStateByIds.GetLotStateByIdsHandler
import com.procurement.access.infrastructure.handler.get.lotids.FindLotIdsHandler
import com.procurement.access.infrastructure.handler.get.organization.GetOrganizationHandler
import com.procurement.access.infrastructure.handler.get.tender.procurement.GetMainProcurementCategoryHandler
import com.procurement.access.infrastructure.handler.get.tender.state.GetTenderStateHandler
import com.procurement.access.infrastructure.handler.pn.OutsourcingPNHandler
import com.procurement.access.infrastructure.handler.processing.responder.ResponderProcessingHandler
import com.procurement.access.infrastructure.handler.set.stateforlots.SetStateForLotsHandler
import com.procurement.access.infrastructure.handler.set.statefortender.SetStateForTenderHandler
import com.procurement.access.infrastructure.handler.v2.CommandDescriptor
import com.procurement.access.infrastructure.handler.validate.ValidateRequirementResponsesHandler
import com.procurement.access.infrastructure.handler.validate.tender.ValidateClassificationHandler
import com.procurement.access.infrastructure.handler.verify.VerifyRequirementResponseHandler
import com.procurement.access.model.dto.bpe.errorResponse
import org.springframework.stereotype.Service

@Service
class CommandService2(
    private val calculateAPValueHandler: CalculateAPValueHandler,
    private val checkAccessToTenderHandler: CheckAccessToTenderHandler,
    private val checkEqualPNAndAPCurrencyHandler: CheckEqualityCurrenciesHandler,
    private val checkExistenceFAHandler: CheckExistenceFAHandler,
    private val checkExistenceSignAuctionHandler: CheckExistenceSignAuctionHandler,
    private val checkPersonsStructureHandler: CheckPersonsStructureHandler,
    private val checkRelationHandler: CheckRelationHandler,
    private val checkTenderStateHandler: CheckTenderStateHandler,
    private val createCriteriaForProcuringEntityHandler: CreateCriteriaForProcuringEntityHandler,
    private val createRelationToOtherProcessHandler: CreateRelationToOtherProcessHandler,
    private val findAuctionsHandler: FindAuctionsHandler,
    private val findCriteriaHandler: FindCriteriaHandler,
    private val findLotIdsHandler: FindLotIdsHandler,
    private val getCurrencyHandler: GetCurrencyHandler,
    private val getLotStateByIdsHandler: GetLotStateByIdsHandler,
    private val getMainProcurementCategoryHandler: GetMainProcurementCategoryHandler,
    private val getOrganizationHandler: GetOrganizationHandler,
    private val getQualificationCriteriaAndMethodHandler: GetQualificationCriteriaAndMethodHandler,
    private val getTenderStateHandler: GetTenderStateHandler,
    private val outsourcingPNHandler: OutsourcingPNHandler,
    private val responderProcessingHandler: ResponderProcessingHandler,
    private val setStateForLotsHandler: SetStateForLotsHandler,
    private val setStateForTenderHandler: SetStateForTenderHandler,
    private val validateClassificationHandler: ValidateClassificationHandler,
    private val validateRequirementResponsesHandler: ValidateRequirementResponsesHandler,
    private val verifyRequirementResponseHandler: VerifyRequirementResponseHandler
) {

        fun execute(descriptor: CommandDescriptor): ApiResponseV2 =
            when (descriptor.action) {
                    CommandTypeV2.CALCULATE_AP_VALUE -> calculateAPValueHandler.handle(descriptor)
                    CommandTypeV2.CHECK_ACCESS_TO_TENDER -> checkAccessToTenderHandler.handle(descriptor)
                    CommandTypeV2.CHECK_EQUALITY_CURRENCIES -> checkEqualPNAndAPCurrencyHandler.handle(descriptor)
                    CommandTypeV2.CHECK_EXISTENCE_FA -> checkExistenceFAHandler.handle(descriptor)
                    CommandTypeV2.CHECK_EXISTENCE_SIGN_AUCTION -> checkExistenceSignAuctionHandler.handle(descriptor)
                    CommandTypeV2.CHECK_PERSONES_STRUCTURE -> checkPersonsStructureHandler.handle(descriptor)
                    CommandTypeV2.CHECK_RELATION -> checkRelationHandler.handle(descriptor)
                    CommandTypeV2.CHECK_TENDER_STATE -> checkTenderStateHandler.handle(descriptor)
                    CommandTypeV2.CREATE_CRITERIA_FOR_PROCURING_ENTITY -> createCriteriaForProcuringEntityHandler.handle(descriptor)
                    CommandTypeV2.CREATE_RELATION_TO_OTHER_PROCESS -> createRelationToOtherProcessHandler.handle(descriptor)
                    CommandTypeV2.FIND_AUCTIONS -> findAuctionsHandler.handle(descriptor)
                    CommandTypeV2.FIND_CRITERIA -> findCriteriaHandler.handle(descriptor)
                    CommandTypeV2.FIND_LOT_IDS -> findLotIdsHandler.handle(descriptor)
                    CommandTypeV2.GET_CURRENCY -> getCurrencyHandler.handle(descriptor)
                    CommandTypeV2.GET_LOT_STATE_BY_IDS -> getLotStateByIdsHandler.handle(descriptor)
                    CommandTypeV2.GET_MAIN_PROCUREMENT_CATEGORY -> getMainProcurementCategoryHandler.handle(descriptor)
                    CommandTypeV2.GET_ORGANIZATION -> getOrganizationHandler.handle(descriptor)
                    CommandTypeV2.GET_QUALIFICATION_CRITERIA_AND_METHOD -> getQualificationCriteriaAndMethodHandler.handle(descriptor)
                    CommandTypeV2.GET_TENDER_STATE -> getTenderStateHandler.handle(descriptor)
                    CommandTypeV2.OUTSOURCING_PN -> outsourcingPNHandler.handle(descriptor)
                    CommandTypeV2.RESPONDER_PROCESSING -> responderProcessingHandler.handle(descriptor)
                    CommandTypeV2.SET_STATE_FOR_LOTS -> setStateForLotsHandler.handle(descriptor)
                    CommandTypeV2.SET_STATE_FOR_TENDER -> setStateForTenderHandler.handle(descriptor)
                    CommandTypeV2.VALIDATE_CLASSIFICATION -> validateClassificationHandler.handle(descriptor)
                    CommandTypeV2.VALIDATE_REQUIREMENT_RESPONSES -> validateRequirementResponsesHandler.handle(descriptor)
                    CommandTypeV2.VERIFY_REQUIREMENT_RESPONSE -> verifyRequirementResponseHandler.handle(descriptor)

                    else -> {
                            val errorDescription = "Unknown action '${descriptor.action.key}'."
                            errorResponse(
                                fail = BadRequest(description = errorDescription, exception = RuntimeException(errorDescription)),
                                id = descriptor.id,
                                version = descriptor.version
                            )
                    }
            }
}
