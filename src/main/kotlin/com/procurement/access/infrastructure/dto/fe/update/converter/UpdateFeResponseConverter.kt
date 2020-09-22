package com.procurement.access.infrastructure.dto.fe.update.converter

import com.procurement.access.application.service.fe.update.UpdateFEResult
import com.procurement.access.infrastructure.dto.fe.update.UpdateFEResponse

fun UpdateFEResult.convert(): UpdateFEResponse =
    UpdateFEResponse(tender = this.tender.convert())

fun UpdateFEResult.Tender.convert(): UpdateFEResponse.Tender =
    UpdateFEResponse.Tender(
        id = this.id,
        status = this.status,
        statusDetails = this.statusDetails,
        title = this.title,
        description = this.description,
        classification = this.classification.convert(),
        acceleratedProcedure = this.acceleratedProcedure.convert(),
        designContest = this.designContest.convert(),
        electronicWorkflows = this.electronicWorkflows.convert(),
        jointProcurement = this.jointProcurement.convert(),
        procedureOutsourcing = this.procedureOutsourcing.convert(),
        framework = this.framework.convert(),
        dynamicPurchasingSystem = this.dynamicPurchasingSystem.convert(),
        legalBasis = this.legalBasis,
        procurementMethod = this.procurementMethod,
        procurementMethodDetails = this.procurementMethodDetails,
        procurementMethodRationale = this.procurementMethodRationale,
        eligibilityCriteria = this.eligibilityCriteria,
        procuringEntity = this.procuringEntity.convert(),
        requiresElectronicCatalogue = this.requiresElectronicCatalogue,
        submissionMethod = this.submissionMethod.toList(),
        submissionMethodRationale = this.submissionMethodRationale.toList(),
        submissionMethodDetails = this.submissionMethodDetails,
        documents = this.documents?.map { it.convert() },
        procurementMethodModalities = this.procurementMethodModalities,
        mainProcurementCategory = this.mainProcurementCategory,
        value = this.value,
        secondStage = this.secondStage?.convert(),
        otherCriteria = this.otherCriteria.convert(),
        contractPeriod = this.contractPeriod.convert(),
        criteria = this.criteria?.map { it.convert() }
    )

fun UpdateFEResult.Tender.Classification.convert(): UpdateFEResponse.Tender.Classification =
    UpdateFEResponse.Tender.Classification(
        scheme = this.scheme,
        id = this.id,
        description = this.description
    )

fun UpdateFEResult.Tender.AcceleratedProcedure.convert(): UpdateFEResponse.Tender.AcceleratedProcedure =
    UpdateFEResponse.Tender.AcceleratedProcedure(
        isAcceleratedProcedure = this.isAcceleratedProcedure
    )

fun UpdateFEResult.Tender.DesignContest.convert(): UpdateFEResponse.Tender.DesignContest =
    UpdateFEResponse.Tender.DesignContest(
        serviceContractAward = this.serviceContractAward
    )

fun UpdateFEResult.Tender.ElectronicWorkflows.convert(): UpdateFEResponse.Tender.ElectronicWorkflows =
    UpdateFEResponse.Tender.ElectronicWorkflows(
        useOrdering = this.useOrdering,
        usePayment = this.usePayment,
        acceptInvoicing = this.acceptInvoicing
    )

fun UpdateFEResult.Tender.JointProcurement.convert(): UpdateFEResponse.Tender.JointProcurement =
    UpdateFEResponse.Tender.JointProcurement(
        isJointProcurement = this.isJointProcurement
    )

fun UpdateFEResult.Tender.ProcedureOutsourcing.convert(): UpdateFEResponse.Tender.ProcedureOutsourcing =
    UpdateFEResponse.Tender.ProcedureOutsourcing(
        procedureOutsourced = this.procedureOutsourced
    )

fun UpdateFEResult.Tender.Framework.convert(): UpdateFEResponse.Tender.Framework =
    UpdateFEResponse.Tender.Framework(
        isAFramework = this.isAFramework
    )

fun UpdateFEResult.Tender.DynamicPurchasingSystem.convert(): UpdateFEResponse.Tender.DynamicPurchasingSystem =
    UpdateFEResponse.Tender.DynamicPurchasingSystem(
        hasDynamicPurchasingSystem = this.hasDynamicPurchasingSystem
    )

fun UpdateFEResult.Tender.Document.convert(): UpdateFEResponse.Tender.Document =
    UpdateFEResponse.Tender.Document(
        documentType = this.documentType,
        id = this.id,
        title = this.title,
        description = this.description
    )

fun UpdateFEResult.Tender.SecondStage.convert(): UpdateFEResponse.Tender.SecondStage =
    UpdateFEResponse.Tender.SecondStage(
        minimumCandidates = this.minimumCandidates,
        maximumCandidates = this.maximumCandidates
    )

fun UpdateFEResult.Tender.OtherCriteria.convert(): UpdateFEResponse.Tender.OtherCriteria =
    UpdateFEResponse.Tender.OtherCriteria(
        reductionCriteria = this.reductionCriteria,
        qualificationSystemMethods = this.qualificationSystemMethods
    )

fun UpdateFEResult.Tender.ContractPeriod.convert(): UpdateFEResponse.Tender.ContractPeriod =
    UpdateFEResponse.Tender.ContractPeriod(
        startDate = this.startDate,
        endDate = this.endDate
    )

fun UpdateFEResult.Tender.Criteria.convert(): UpdateFEResponse.Tender.Criteria =
    UpdateFEResponse.Tender.Criteria(
        id = this.id,
        title = this.title,
        description = this.description,
        relatesTo = this.relatesTo,
        source = this.source,
        requirementGroups = this.requirementGroups.map { it.convert() }
    )

fun UpdateFEResult.Tender.Criteria.RequirementGroup.convert(): UpdateFEResponse.Tender.Criteria.RequirementGroup =
    UpdateFEResponse.Tender.Criteria.RequirementGroup(
        id = this.id,
        description = this.description,
        requirements = this.requirements
    )

fun UpdateFEResult.Tender.ProcuringEntity.convert(): UpdateFEResponse.Tender.ProcuringEntity =
    UpdateFEResponse.Tender.ProcuringEntity(
        id = this.id,
        name = this.name,
        identifier = this.identifier.convert(),
        additionalIdentifiers = this.additionalIdentifiers?.map { it.convert() },
        address = this.address.convert(),
        contactPoint = this.contactPoint.convert(),
        persons = this.persons.map { it.convert() }
    )

fun UpdateFEResult.Tender.ProcuringEntity.Identifier.convert(): UpdateFEResponse.Tender.ProcuringEntity.Identifier =
    UpdateFEResponse.Tender.ProcuringEntity.Identifier(
        scheme = this.scheme,
        id = this.id,
        legalName = this.legalName,
        uri = this.uri
    )

fun UpdateFEResult.Tender.ProcuringEntity.Address.convert(): UpdateFEResponse.Tender.ProcuringEntity.Address =
    UpdateFEResponse.Tender.ProcuringEntity.Address(
        streetAddress = this.streetAddress,
        postalCode = this.postalCode,
        addressDetails = this.addressDetails.convert()
    )

fun UpdateFEResult.Tender.ProcuringEntity.Address.AddressDetails.convert(): UpdateFEResponse.Tender.ProcuringEntity.Address.AddressDetails =
    UpdateFEResponse.Tender.ProcuringEntity.Address.AddressDetails(
        country = this.country.convert(),
        region = this.region.convert(),
        locality = this.locality.convert()
    )

fun UpdateFEResult.Tender.ProcuringEntity.Address.AddressDetails.Country.convert(): UpdateFEResponse.Tender.ProcuringEntity.Address.AddressDetails.Country =
    UpdateFEResponse.Tender.ProcuringEntity.Address.AddressDetails.Country(
        scheme = this.scheme,
        id = this.id,
        description = this.description,
        uri = this.uri
    )

fun UpdateFEResult.Tender.ProcuringEntity.Address.AddressDetails.Region.convert(): UpdateFEResponse.Tender.ProcuringEntity.Address.AddressDetails.Region =
    UpdateFEResponse.Tender.ProcuringEntity.Address.AddressDetails.Region(
        scheme = this.scheme,
        id = this.id,
        description = this.description,
        uri = this.uri
    )

fun UpdateFEResult.Tender.ProcuringEntity.Address.AddressDetails.Locality.convert(): UpdateFEResponse.Tender.ProcuringEntity.Address.AddressDetails.Locality =
    UpdateFEResponse.Tender.ProcuringEntity.Address.AddressDetails.Locality(
        scheme = this.scheme,
        id = this.id,
        description = this.description,
        uri = this.uri
    )

fun UpdateFEResult.Tender.ProcuringEntity.ContactPoint.convert(): UpdateFEResponse.Tender.ProcuringEntity.ContactPoint =
    UpdateFEResponse.Tender.ProcuringEntity.ContactPoint(
        name = this.name,
        email = this.email,
        telephone = this.telephone,
        faxNumber = this.faxNumber,
        url = this.url
    )

fun UpdateFEResult.Tender.ProcuringEntity.Person.convert(): UpdateFEResponse.Tender.ProcuringEntity.Person =
    UpdateFEResponse.Tender.ProcuringEntity.Person(
        id = this.id,
        title = this.title,
        name = this.name,
        identifier = this.identifier.convert(),
        businessFunctions = this.businessFunctions.map { it.convert() }
    )

fun UpdateFEResult.Tender.ProcuringEntity.Person.Identifier.convert(): UpdateFEResponse.Tender.ProcuringEntity.Person.Identifier =
    UpdateFEResponse.Tender.ProcuringEntity.Person.Identifier(
        id = this.id,
        scheme = this.scheme,
        uri = this.uri
    )

fun UpdateFEResult.Tender.ProcuringEntity.Person.BusinessFunction.convert(): UpdateFEResponse.Tender.ProcuringEntity.Person.BusinessFunction =
    UpdateFEResponse.Tender.ProcuringEntity.Person.BusinessFunction(
        id = this.id,
        jobTitle = this.jobTitle,
        type = this.type,
        period = this.period.convert(),
        documents = this.documents?.map { it.convert() }
    )

fun UpdateFEResult.Tender.ProcuringEntity.Person.BusinessFunction.Document.convert(): UpdateFEResponse.Tender.ProcuringEntity.Person.BusinessFunction.Document =
    UpdateFEResponse.Tender.ProcuringEntity.Person.BusinessFunction.Document(
        id = this.id,
        documentType = this.documentType,
        title = this.title,
        description = this.description
    )

fun UpdateFEResult.Tender.ProcuringEntity.Person.BusinessFunction.Period.convert(): UpdateFEResponse.Tender.ProcuringEntity.Person.BusinessFunction.Period =
    UpdateFEResponse.Tender.ProcuringEntity.Person.BusinessFunction.Period(
        startDate = this.startDate
    )