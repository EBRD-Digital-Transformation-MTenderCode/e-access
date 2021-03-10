package com.procurement.access.service

import com.procurement.access.application.model.context.CheckFEDataContext
import com.procurement.access.application.service.command.CheckFEDataRules
import com.procurement.access.application.service.command.CheckFEDataRules.Companion.isNeedValidate
import com.procurement.access.application.service.fe.check.CheckFEDataData
import com.procurement.access.dao.TenderProcessDao
import org.springframework.stereotype.Service

interface FeValidationService {
    fun checkFEData(context: CheckFEDataContext, data: CheckFEDataData)
}

@Service
class FeValidationServiceImpl(private val tenderProcessDao: TenderProcessDao) : FeValidationService {

    override fun checkFEData(context: CheckFEDataContext, data: CheckFEDataData) {

        // FR.COM-1.27.1
        CheckFEDataRules.validateTitle(data.tender.title)

        // FR.COM-1.27.2
        CheckFEDataRules.validateDescription(data.tender.description)

        // FR.COM-1.27.3
        data.tender.procuringEntity?.let { procuringEntity ->
            val entity = CheckFEDataRules.getEntity(tenderProcessDao, context)

            // VR-1.0.1.10.1
            CheckFEDataRules.validateProcuringEntity(context.operationType, entity, data.tender.procuringEntity)

            // VR-1.0.1.10.2
            CheckFEDataRules.validatePersonsExistence(procuringEntity.persons)

            // FReq-1.1.1.16
            // VR-1.0.1.10.3
            CheckFEDataRules.validateUniquenessBy(procuringEntity.persons, "procuringEntity.persones[]") { it.identifier.id }

            // VR-1.0.1.10.4
            CheckFEDataRules.validatePersonsBusinessFunctions(procuringEntity.persons)

            procuringEntity.persons.forEach { person ->

                // FReq-1.1.1.16
                // VR-1.0.1.10.5
                CheckFEDataRules.validateUniquenessBy(person.businessFunctions, "procuringEntity.persones.businessFunctions") { it.id }

                person.businessFunctions.forEach { businessFunction ->
                    // VR-1.0.1.10.7
                    CheckFEDataRules.validateBusinessFunctionsPeriod(context.startDate, businessFunction.period)

                    // FReq-1.1.1.16
                    // VR-1.0.1.2.1
                    CheckFEDataRules.validateUniquenessBy(businessFunction.documents, "tender.businessFunctions[].documents[]") { it.id }
                }
            }
        }

        // FReq-1.1.1.16
        CheckFEDataRules.validateUniquenessBy(data.tender.documents, "tender.documents[]") { it.id }  // VR-1.0.1.2.1

        // FR.COM-1.27.5
        val receivedSecondStage = data.tender.secondStage
        if (receivedSecondStage.isNeedValidate(context.operationType))
            receivedSecondStage?.let { secondStage ->
                // VR-1.0.1.11.1
                CheckFEDataRules.validateSecondStageAttributesExistance(secondStage)

                // VR-1.0.1.11.2
                secondStage.minimumCandidates?.let { CheckFEDataRules.validateMinimumCandidates(it) }

                // VR-1.0.1.11.3
                secondStage.maximumCandidates?.let { CheckFEDataRules.validateMaximumCandidates(it) }

                // VR-1.0.1.11.4
                CheckFEDataRules.validateCandidates(secondStage)
            }

        // FR.COM-1.27.6
        data.tender.criteria
            .takeIf { it.isNeedValidate(context.operationType) && it.isNotEmpty() }
            ?.let { criteria ->

                // FReq-1.1.1.16
                CheckFEDataRules.validateUniquenessBy(criteria, "tender.criteria[]") { it.id }

                // FReq-1.1.1.4
                CheckFEDataRules.checkDatatypeCompliance(criteria)

                // FReq-1.1.1.5
                CheckFEDataRules.checkMinMaxValue(criteria)

                // FReq-1.1.1.6
                CheckFEDataRules.checkDateTime(criteria)

                // FReq-1.1.1.8
                CheckFEDataRules.checkRequirements(criteria)

                // FReq-1.1.1.29
                CheckFEDataRules.checkCriteriaRelation(criteria.map { it.relatesTo })

                // FReq-1.1.1.25
                CheckFEDataRules.checkCriteriaValueScale(criteria)

                // FReq-1.1.1.32
                CheckFEDataRules.checkCriteriaCategory(criteria)

                //FReq-1.1.1.33, FReq-1.1.1.34
                CheckFEDataRules.checkClassification(criteria, data.criteria)

                //FReq-1.1.1.37, FReq-1.1.1.38
                CheckFEDataRules.checkEligibleEvidences(criteria, data.tender.documents)
            }


        // FR.COM-1.27.7
        // VR-1.0.1.12.1
        val otherCriteria = data.tender.otherCriteria
        if (otherCriteria.isNeedValidate(context.operationType))
            otherCriteria.let { CheckFEDataRules.checkOtherCriteria(it) }

        // FR.COM-1.27.8
        CheckFEDataRules.checkCriteriaReference(data, context)
    }
}
