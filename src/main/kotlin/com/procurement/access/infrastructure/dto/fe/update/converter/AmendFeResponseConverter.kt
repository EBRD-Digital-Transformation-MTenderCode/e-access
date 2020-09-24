package com.procurement.access.infrastructure.dto.fe.update.converter

import com.procurement.access.application.service.fe.update.AmendFEResult
import com.procurement.access.infrastructure.dto.fe.update.AmendFEResponse

fun AmendFEResult.convert(): AmendFEResponse =
    AmendFEResponse(tender = this.tender.convert())

fun AmendFEResult.Tender.convert(): AmendFEResponse.Tender =
    AmendFEResponse.Tender(
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

fun AmendFEResult.Tender.Classification.convert(): AmendFEResponse.Tender.Classification =
    AmendFEResponse.Tender.Classification(
        scheme = this.scheme,
        id = this.id,
        description = this.description
    )

fun AmendFEResult.Tender.AcceleratedProcedure.convert(): AmendFEResponse.Tender.AcceleratedProcedure =
    AmendFEResponse.Tender.AcceleratedProcedure(
        isAcceleratedProcedure = this.isAcceleratedProcedure
    )

fun AmendFEResult.Tender.DesignContest.convert(): AmendFEResponse.Tender.DesignContest =
    AmendFEResponse.Tender.DesignContest(
        serviceContractAward = this.serviceContractAward
    )

fun AmendFEResult.Tender.ElectronicWorkflows.convert(): AmendFEResponse.Tender.ElectronicWorkflows =
    AmendFEResponse.Tender.ElectronicWorkflows(
        useOrdering = this.useOrdering,
        usePayment = this.usePayment,
        acceptInvoicing = this.acceptInvoicing
    )

fun AmendFEResult.Tender.JointProcurement.convert(): AmendFEResponse.Tender.JointProcurement =
    AmendFEResponse.Tender.JointProcurement(
        isJointProcurement = this.isJointProcurement
    )

fun AmendFEResult.Tender.ProcedureOutsourcing.convert(): AmendFEResponse.Tender.ProcedureOutsourcing =
    AmendFEResponse.Tender.ProcedureOutsourcing(
        procedureOutsourced = this.procedureOutsourced
    )

fun AmendFEResult.Tender.Framework.convert(): AmendFEResponse.Tender.Framework =
    AmendFEResponse.Tender.Framework(
        isAFramework = this.isAFramework
    )

fun AmendFEResult.Tender.DynamicPurchasingSystem.convert(): AmendFEResponse.Tender.DynamicPurchasingSystem =
    AmendFEResponse.Tender.DynamicPurchasingSystem(
        hasDynamicPurchasingSystem = this.hasDynamicPurchasingSystem
    )

fun AmendFEResult.Tender.Document.convert(): AmendFEResponse.Tender.Document =
    AmendFEResponse.Tender.Document(
        documentType = this.documentType,
        id = this.id,
        title = this.title,
        description = this.description
    )

fun AmendFEResult.Tender.SecondStage.convert(): AmendFEResponse.Tender.SecondStage =
    AmendFEResponse.Tender.SecondStage(
        minimumCandidates = this.minimumCandidates,
        maximumCandidates = this.maximumCandidates
    )

fun AmendFEResult.Tender.OtherCriteria.convert(): AmendFEResponse.Tender.OtherCriteria =
    AmendFEResponse.Tender.OtherCriteria(
        reductionCriteria = this.reductionCriteria,
        qualificationSystemMethods = this.qualificationSystemMethods
    )

fun AmendFEResult.Tender.ContractPeriod.convert(): AmendFEResponse.Tender.ContractPeriod =
    AmendFEResponse.Tender.ContractPeriod(
        startDate = this.startDate,
        endDate = this.endDate
    )

fun AmendFEResult.Tender.Criteria.convert(): AmendFEResponse.Tender.Criteria =
    AmendFEResponse.Tender.Criteria(
        id = this.id,
        title = this.title,
        description = this.description,
        relatesTo = this.relatesTo,
        source = this.source,
        requirementGroups = this.requirementGroups.map { it.convert() }
    )

fun AmendFEResult.Tender.Criteria.RequirementGroup.convert(): AmendFEResponse.Tender.Criteria.RequirementGroup =
    AmendFEResponse.Tender.Criteria.RequirementGroup(
        id = this.id,
        description = this.description,
        requirements = this.requirements
    )

fun AmendFEResult.Tender.ProcuringEntity.convert(): AmendFEResponse.Tender.ProcuringEntity =
    AmendFEResponse.Tender.ProcuringEntity(
        id = this.id,
        name = this.name,
        identifier = this.identifier.convert(),
        additionalIdentifiers = this.additionalIdentifiers?.map { it.convert() },
        address = this.address.convert(),
        contactPoint = this.contactPoint.convert(),
        persons = this.persons.map { it.convert() }
    )

fun AmendFEResult.Tender.ProcuringEntity.Identifier.convert(): AmendFEResponse.Tender.ProcuringEntity.Identifier =
    AmendFEResponse.Tender.ProcuringEntity.Identifier(
        scheme = this.scheme,
        id = this.id,
        legalName = this.legalName,
        uri = this.uri
    )

fun AmendFEResult.Tender.ProcuringEntity.Address.convert(): AmendFEResponse.Tender.ProcuringEntity.Address =
    AmendFEResponse.Tender.ProcuringEntity.Address(
        streetAddress = this.streetAddress,
        postalCode = this.postalCode,
        addressDetails = this.addressDetails.convert()
    )

fun AmendFEResult.Tender.ProcuringEntity.Address.AddressDetails.convert(): AmendFEResponse.Tender.ProcuringEntity.Address.AddressDetails =
    AmendFEResponse.Tender.ProcuringEntity.Address.AddressDetails(
        country = this.country.convert(),
        region = this.region.convert(),
        locality = this.locality.convert()
    )

fun AmendFEResult.Tender.ProcuringEntity.Address.AddressDetails.Country.convert(): AmendFEResponse.Tender.ProcuringEntity.Address.AddressDetails.Country =
    AmendFEResponse.Tender.ProcuringEntity.Address.AddressDetails.Country(
        scheme = this.scheme,
        id = this.id,
        description = this.description,
        uri = this.uri
    )

fun AmendFEResult.Tender.ProcuringEntity.Address.AddressDetails.Region.convert(): AmendFEResponse.Tender.ProcuringEntity.Address.AddressDetails.Region =
    AmendFEResponse.Tender.ProcuringEntity.Address.AddressDetails.Region(
        scheme = this.scheme,
        id = this.id,
        description = this.description,
        uri = this.uri
    )

fun AmendFEResult.Tender.ProcuringEntity.Address.AddressDetails.Locality.convert(): AmendFEResponse.Tender.ProcuringEntity.Address.AddressDetails.Locality =
    AmendFEResponse.Tender.ProcuringEntity.Address.AddressDetails.Locality(
        scheme = this.scheme,
        id = this.id,
        description = this.description,
        uri = this.uri
    )

fun AmendFEResult.Tender.ProcuringEntity.ContactPoint.convert(): AmendFEResponse.Tender.ProcuringEntity.ContactPoint =
    AmendFEResponse.Tender.ProcuringEntity.ContactPoint(
        name = this.name,
        email = this.email,
        telephone = this.telephone,
        faxNumber = this.faxNumber,
        url = this.url
    )

fun AmendFEResult.Tender.ProcuringEntity.Person.convert(): AmendFEResponse.Tender.ProcuringEntity.Person =
    AmendFEResponse.Tender.ProcuringEntity.Person(
        id = this.id,
        title = this.title,
        name = this.name,
        identifier = this.identifier.convert(),
        businessFunctions = this.businessFunctions.map { it.convert() }
    )

fun AmendFEResult.Tender.ProcuringEntity.Person.Identifier.convert(): AmendFEResponse.Tender.ProcuringEntity.Person.Identifier =
    AmendFEResponse.Tender.ProcuringEntity.Person.Identifier(
        id = this.id,
        scheme = this.scheme,
        uri = this.uri
    )

fun AmendFEResult.Tender.ProcuringEntity.Person.BusinessFunction.convert(): AmendFEResponse.Tender.ProcuringEntity.Person.BusinessFunction =
    AmendFEResponse.Tender.ProcuringEntity.Person.BusinessFunction(
        id = this.id,
        jobTitle = this.jobTitle,
        type = this.type,
        period = this.period.convert(),
        documents = this.documents?.map { it.convert() }
    )

fun AmendFEResult.Tender.ProcuringEntity.Person.BusinessFunction.Document.convert(): AmendFEResponse.Tender.ProcuringEntity.Person.BusinessFunction.Document =
    AmendFEResponse.Tender.ProcuringEntity.Person.BusinessFunction.Document(
        id = this.id,
        documentType = this.documentType,
        title = this.title,
        description = this.description
    )

fun AmendFEResult.Tender.ProcuringEntity.Person.BusinessFunction.Period.convert(): AmendFEResponse.Tender.ProcuringEntity.Person.BusinessFunction.Period =
    AmendFEResponse.Tender.ProcuringEntity.Person.BusinessFunction.Period(
        startDate = this.startDate
    )