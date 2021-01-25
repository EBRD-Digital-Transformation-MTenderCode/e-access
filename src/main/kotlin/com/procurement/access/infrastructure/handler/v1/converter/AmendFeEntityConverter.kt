package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.service.fe.update.AmendFEResult
import com.procurement.access.infrastructure.entity.FEEntity

class AmendFeEntityConverter {
    companion object {

        fun fromEntity(entity: FEEntity): AmendFEResult =
            AmendFEResult(tender = fromEntity(entity.tender))

        private fun fromEntity(entity: FEEntity.Tender): AmendFEResult.Tender =
            AmendFEResult.Tender(
                id = entity.id,
                status = entity.status,
                statusDetails = entity.statusDetails,
                title = entity.title,
                description = entity.description,
                classification = convert(entity.classification),
                acceleratedProcedure = convert(entity.acceleratedProcedure),
                designContest = convert(entity.designContest),
                electronicWorkflows = convert(entity.electronicWorkflows),
                jointProcurement = convert(entity.jointProcurement),
                procedureOutsourcing = convert(entity.procedureOutsourcing),
                framework = convert(entity.framework),
                dynamicPurchasingSystem = convert(entity.dynamicPurchasingSystem),
                legalBasis = entity.legalBasis,
                procurementMethod = entity.procurementMethod,
                procurementMethodDetails = entity.procurementMethodDetails,
                procurementMethodRationale = entity.procurementMethodRationale,
                eligibilityCriteria = entity.eligibilityCriteria,
                procuringEntity = convert(entity.procuringEntity!!),
                requiresElectronicCatalogue = entity.requiresElectronicCatalogue,
                submissionMethod = entity.submissionMethod.toList(),
                submissionMethodRationale = entity.submissionMethodRationale.toList(),
                submissionMethodDetails = entity.submissionMethodDetails,
                documents = entity.documents
                    ?.map { convert(it) }
                    .orEmpty(),
                procurementMethodModalities = entity.procurementMethodModalities.orEmpty(),
                mainProcurementCategory = entity.mainProcurementCategory,
                value = entity.value,
                secondStage = entity.secondStage
                    ?.let { convert(it) },
                otherCriteria = convert(entity.otherCriteria!!),
                contractPeriod = convert(entity.contractPeriod),
                criteria = entity.criteria
                    ?.map { convert(it) }
                    .orEmpty()
            )

        private fun convert(entity: FEEntity.Tender.Classification): AmendFEResult.Tender.Classification =
            AmendFEResult.Tender.Classification(
                scheme = entity.scheme,
                id = entity.id,
                description = entity.description
            )

        private fun convert(entity: FEEntity.Tender.AcceleratedProcedure): AmendFEResult.Tender.AcceleratedProcedure =
            AmendFEResult.Tender.AcceleratedProcedure(
                isAcceleratedProcedure = entity.isAcceleratedProcedure
            )

        private fun convert(entity: FEEntity.Tender.DesignContest): AmendFEResult.Tender.DesignContest =
            AmendFEResult.Tender.DesignContest(
                serviceContractAward = entity.serviceContractAward
            )

        private fun convert(entity: FEEntity.Tender.ElectronicWorkflows): AmendFEResult.Tender.ElectronicWorkflows =
            AmendFEResult.Tender.ElectronicWorkflows(
                useOrdering = entity.useOrdering,
                usePayment = entity.usePayment,
                acceptInvoicing = entity.acceptInvoicing
            )

        private fun convert(entity: FEEntity.Tender.JointProcurement): AmendFEResult.Tender.JointProcurement =
            AmendFEResult.Tender.JointProcurement(
                isJointProcurement = entity.isJointProcurement
            )

        private fun convert(entity: FEEntity.Tender.ProcedureOutsourcing): AmendFEResult.Tender.ProcedureOutsourcing =
            AmendFEResult.Tender.ProcedureOutsourcing(
                procedureOutsourced = entity.procedureOutsourced
            )

        private fun convert(entity: FEEntity.Tender.Framework): AmendFEResult.Tender.Framework =
            AmendFEResult.Tender.Framework(
                isAFramework = entity.isAFramework
            )

        private fun convert(entity: FEEntity.Tender.DynamicPurchasingSystem): AmendFEResult.Tender.DynamicPurchasingSystem =
            AmendFEResult.Tender.DynamicPurchasingSystem(
                hasDynamicPurchasingSystem = entity.hasDynamicPurchasingSystem
            )

        private fun convert(entity: FEEntity.Tender.Document): AmendFEResult.Tender.Document =
            AmendFEResult.Tender.Document(
                documentType = entity.documentType,
                id = entity.id,
                title = entity.title,
                description = entity.description
            )

        private fun convert(entity: FEEntity.Tender.SecondStage): AmendFEResult.Tender.SecondStage =
            AmendFEResult.Tender.SecondStage(
                minimumCandidates = entity.minimumCandidates,
                maximumCandidates = entity.maximumCandidates
            )

        private fun convert(entity: FEEntity.Tender.OtherCriteria): AmendFEResult.Tender.OtherCriteria =
            AmendFEResult.Tender.OtherCriteria(
                reductionCriteria = entity.reductionCriteria,
                qualificationSystemMethods = entity.qualificationSystemMethods
            )

        private fun convert(entity: FEEntity.Tender.ContractPeriod): AmendFEResult.Tender.ContractPeriod =
            AmendFEResult.Tender.ContractPeriod(
                startDate = entity.startDate,
                endDate = entity.endDate
            )

        private fun convert(entity: FEEntity.Tender.Criteria): AmendFEResult.Tender.Criteria =
            AmendFEResult.Tender.Criteria(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                relatesTo = entity.relatesTo,
                source = entity.source,
                classification = entity.classification?.let { convert(it) },
                requirementGroups = entity.requirementGroups.map { convert(it) }
            )

        private fun convert(entity: FEEntity.Tender.Criteria.Classification): AmendFEResult.Tender.Criteria.Classification =
            AmendFEResult.Tender.Criteria.Classification(
                id = entity.id,
                scheme = entity.scheme
            )

        private fun convert(entity: FEEntity.Tender.Criteria.RequirementGroup): AmendFEResult.Tender.Criteria.RequirementGroup =
            AmendFEResult.Tender.Criteria.RequirementGroup(
                id = entity.id,
                description = entity.description,
                requirements = entity.requirements
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity): AmendFEResult.Tender.ProcuringEntity =
            AmendFEResult.Tender.ProcuringEntity(
                id = entity.id,
                name = entity.name,
                identifier = convert(entity.identifier),
                additionalIdentifiers = entity.additionalIdentifiers
                    ?.map { convert(it) }
                    .orEmpty(),
                address = convert(entity.address),
                contactPoint = convert(entity.contactPoint),
                persons = entity.persons.map { convert(it) }
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Identifier): AmendFEResult.Tender.ProcuringEntity.Identifier =
            AmendFEResult.Tender.ProcuringEntity.Identifier(
                scheme = entity.scheme,
                id = entity.id,
                legalName = entity.legalName,
                uri = entity.uri
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Address): AmendFEResult.Tender.ProcuringEntity.Address =
            AmendFEResult.Tender.ProcuringEntity.Address(
                streetAddress = entity.streetAddress,
                postalCode = entity.postalCode,
                addressDetails = convert(entity.addressDetails)
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Address.AddressDetails): AmendFEResult.Tender.ProcuringEntity.Address.AddressDetails =
            AmendFEResult.Tender.ProcuringEntity.Address.AddressDetails(
                country = convert(entity.country),
                region = convert(entity.region),
                locality = convert(entity.locality)
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Address.AddressDetails.Country): AmendFEResult.Tender.ProcuringEntity.Address.AddressDetails.Country =
            AmendFEResult.Tender.ProcuringEntity.Address.AddressDetails.Country(
                scheme = entity.scheme,
                id = entity.id,
                description = entity.description,
                uri = entity.uri
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Address.AddressDetails.Region): AmendFEResult.Tender.ProcuringEntity.Address.AddressDetails.Region =
            AmendFEResult.Tender.ProcuringEntity.Address.AddressDetails.Region(
                scheme = entity.scheme,
                id = entity.id,
                description = entity.description,
                uri = entity.uri
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Address.AddressDetails.Locality): AmendFEResult.Tender.ProcuringEntity.Address.AddressDetails.Locality =
            AmendFEResult.Tender.ProcuringEntity.Address.AddressDetails.Locality(
                scheme = entity.scheme,
                id = entity.id,
                description = entity.description,
                uri = entity.uri
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.ContactPoint): AmendFEResult.Tender.ProcuringEntity.ContactPoint =
            AmendFEResult.Tender.ProcuringEntity.ContactPoint(
                name = entity.name,
                email = entity.email,
                telephone = entity.telephone,
                faxNumber = entity.faxNumber,
                url = entity.url
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Person): AmendFEResult.Tender.ProcuringEntity.Person =
            AmendFEResult.Tender.ProcuringEntity.Person(
                id = entity.id,
                title = entity.title,
                name = entity.name,
                identifier = convert(entity.identifier),
                businessFunctions = entity.businessFunctions.map { convert(it) }
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Person.Identifier): AmendFEResult.Tender.ProcuringEntity.Person.Identifier =
            AmendFEResult.Tender.ProcuringEntity.Person.Identifier(
                id = entity.id,
                scheme = entity.scheme,
                uri = entity.uri
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Person.BusinessFunction): AmendFEResult.Tender.ProcuringEntity.Person.BusinessFunction =
            AmendFEResult.Tender.ProcuringEntity.Person.BusinessFunction(
                id = entity.id,
                jobTitle = entity.jobTitle,
                type = entity.type,
                period = convert(entity.period),
                documents = entity.documents
                    ?.map { convert(it) }
                    .orEmpty()
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.Document): AmendFEResult.Tender.ProcuringEntity.Person.BusinessFunction.Document =
            AmendFEResult.Tender.ProcuringEntity.Person.BusinessFunction.Document(
                id = entity.id,
                documentType = entity.documentType,
                title = entity.title,
                description = entity.description
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.Period): AmendFEResult.Tender.ProcuringEntity.Person.BusinessFunction.Period =
            AmendFEResult.Tender.ProcuringEntity.Person.BusinessFunction.Period(
                startDate = entity.startDate
            )
    }
}