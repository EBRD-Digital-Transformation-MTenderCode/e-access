package com.procurement.access.infrastructure.dto.fe.check.converter

import com.procurement.access.application.service.fe.create.CreateFEResult
import com.procurement.access.infrastructure.entity.FEEntity

class CreateFeEntityConverter {
    companion object {

        fun fromEntity(entity: FEEntity): CreateFEResult =
            CreateFEResult(
                ocid = entity.ocid,
                token = entity.token,
                tender = fromEntity(entity.tender)
            )

        private fun fromEntity(entity: FEEntity.Tender): CreateFEResult.Tender =
            CreateFEResult.Tender(
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

        private fun convert(entity: FEEntity.Tender.Classification): CreateFEResult.Tender.Classification =
            CreateFEResult.Tender.Classification(
                scheme = entity.scheme,
                id = entity.id,
                description = entity.description
            )

        private fun convert(entity: FEEntity.Tender.AcceleratedProcedure): CreateFEResult.Tender.AcceleratedProcedure =
            CreateFEResult.Tender.AcceleratedProcedure(
                isAcceleratedProcedure = entity.isAcceleratedProcedure
            )

        private fun convert(entity: FEEntity.Tender.DesignContest): CreateFEResult.Tender.DesignContest =
            CreateFEResult.Tender.DesignContest(
                serviceContractAward = entity.serviceContractAward
            )

        private fun convert(entity: FEEntity.Tender.ElectronicWorkflows): CreateFEResult.Tender.ElectronicWorkflows =
            CreateFEResult.Tender.ElectronicWorkflows(
                useOrdering = entity.useOrdering,
                usePayment = entity.usePayment,
                acceptInvoicing = entity.acceptInvoicing
            )

        private fun convert(entity: FEEntity.Tender.JointProcurement): CreateFEResult.Tender.JointProcurement =
            CreateFEResult.Tender.JointProcurement(
                isJointProcurement = entity.isJointProcurement
            )

        private fun convert(entity: FEEntity.Tender.ProcedureOutsourcing): CreateFEResult.Tender.ProcedureOutsourcing =
            CreateFEResult.Tender.ProcedureOutsourcing(
                procedureOutsourced = entity.procedureOutsourced
            )

        private fun convert(entity: FEEntity.Tender.Framework): CreateFEResult.Tender.Framework =
            CreateFEResult.Tender.Framework(
                isAFramework = entity.isAFramework
            )

        private fun convert(entity: FEEntity.Tender.DynamicPurchasingSystem): CreateFEResult.Tender.DynamicPurchasingSystem =
            CreateFEResult.Tender.DynamicPurchasingSystem(
                hasDynamicPurchasingSystem = entity.hasDynamicPurchasingSystem
            )

        private fun convert(entity: FEEntity.Tender.Document): CreateFEResult.Tender.Document =
            CreateFEResult.Tender.Document(
                documentType = entity.documentType,
                id = entity.id,
                title = entity.title,
                description = entity.description
            )

        private fun convert(entity: FEEntity.Tender.SecondStage): CreateFEResult.Tender.SecondStage =
            CreateFEResult.Tender.SecondStage(
                minimumCandidates = entity.minimumCandidates,
                maximumCandidates = entity.maximumCandidates
            )

        private fun convert(entity: FEEntity.Tender.OtherCriteria): CreateFEResult.Tender.OtherCriteria =
            CreateFEResult.Tender.OtherCriteria(
                reductionCriteria = entity.reductionCriteria,
                qualificationSystemMethods = entity.qualificationSystemMethods
            )

        private fun convert(entity: FEEntity.Tender.ContractPeriod): CreateFEResult.Tender.ContractPeriod =
            CreateFEResult.Tender.ContractPeriod(
                startDate = entity.startDate,
                endDate = entity.endDate
            )

        private fun convert(entity: FEEntity.Tender.Criteria): CreateFEResult.Tender.Criteria =
            CreateFEResult.Tender.Criteria(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                relatesTo = entity.relatesTo,
                source = entity.source,
                requirementGroups = entity.requirementGroups.map { convert(it) }
            )

        private fun convert(entity: FEEntity.Tender.Criteria.RequirementGroup): CreateFEResult.Tender.Criteria.RequirementGroup =
            CreateFEResult.Tender.Criteria.RequirementGroup(
                id = entity.id,
                description = entity.description,
                requirements = entity.requirements
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity): CreateFEResult.Tender.ProcuringEntity =
            CreateFEResult.Tender.ProcuringEntity(
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

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Identifier): CreateFEResult.Tender.ProcuringEntity.Identifier =
            CreateFEResult.Tender.ProcuringEntity.Identifier(
                scheme = entity.scheme,
                id = entity.id,
                legalName = entity.legalName,
                uri = entity.uri
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Address): CreateFEResult.Tender.ProcuringEntity.Address =
            CreateFEResult.Tender.ProcuringEntity.Address(
                streetAddress = entity.streetAddress,
                postalCode = entity.postalCode,
                addressDetails = convert(entity.addressDetails)
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Address.AddressDetails): CreateFEResult.Tender.ProcuringEntity.Address.AddressDetails =
            CreateFEResult.Tender.ProcuringEntity.Address.AddressDetails(
                country = convert(entity.country),
                region = convert(entity.region),
                locality = convert(entity.locality)
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Address.AddressDetails.Country): CreateFEResult.Tender.ProcuringEntity.Address.AddressDetails.Country =
            CreateFEResult.Tender.ProcuringEntity.Address.AddressDetails.Country(
                scheme = entity.scheme,
                id = entity.id,
                description = entity.description,
                uri = entity.uri
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Address.AddressDetails.Region): CreateFEResult.Tender.ProcuringEntity.Address.AddressDetails.Region =
            CreateFEResult.Tender.ProcuringEntity.Address.AddressDetails.Region(
                scheme = entity.scheme,
                id = entity.id,
                description = entity.description,
                uri = entity.uri
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Address.AddressDetails.Locality): CreateFEResult.Tender.ProcuringEntity.Address.AddressDetails.Locality =
            CreateFEResult.Tender.ProcuringEntity.Address.AddressDetails.Locality(
                scheme = entity.scheme,
                id = entity.id,
                description = entity.description,
                uri = entity.uri
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.ContactPoint): CreateFEResult.Tender.ProcuringEntity.ContactPoint =
            CreateFEResult.Tender.ProcuringEntity.ContactPoint(
                name = entity.name,
                email = entity.email,
                telephone = entity.telephone,
                faxNumber = entity.faxNumber,
                url = entity.url
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Person): CreateFEResult.Tender.ProcuringEntity.Person =
            CreateFEResult.Tender.ProcuringEntity.Person(
                id = entity.id,
                title = entity.title,
                name = entity.name,
                identifier = convert(entity.identifier),
                businessFunctions = entity.businessFunctions.map { convert(it) }
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Person.Identifier): CreateFEResult.Tender.ProcuringEntity.Person.Identifier =
            CreateFEResult.Tender.ProcuringEntity.Person.Identifier(
                id = entity.id,
                scheme = entity.scheme,
                uri = entity.uri
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Person.BusinessFunction): CreateFEResult.Tender.ProcuringEntity.Person.BusinessFunction =
            CreateFEResult.Tender.ProcuringEntity.Person.BusinessFunction(
                id = entity.id,
                jobTitle = entity.jobTitle,
                type = entity.type,
                period = convert(entity.period),
                documents = entity.documents
                    ?.map { convert(it) }
                    .orEmpty()
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.Document): CreateFEResult.Tender.ProcuringEntity.Person.BusinessFunction.Document =
            CreateFEResult.Tender.ProcuringEntity.Person.BusinessFunction.Document(
                id = entity.id,
                documentType = entity.documentType,
                title = entity.title,
                description = entity.description
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.Period): CreateFEResult.Tender.ProcuringEntity.Person.BusinessFunction.Period =
            CreateFEResult.Tender.ProcuringEntity.Person.BusinessFunction.Period(
                startDate = entity.startDate
            )
    }
}