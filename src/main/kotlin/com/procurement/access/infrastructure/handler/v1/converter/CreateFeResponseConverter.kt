package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.service.fe.create.CreateFEResult
import com.procurement.access.infrastructure.handler.v1.model.response.CreateFEResponse

fun CreateFEResult.convert(): CreateFEResponse =
    CreateFEResponse(
        ocid = this.ocid,
        token = this.token,
        tender = this.tender.convert()
    )

fun CreateFEResult.Tender.convert(): CreateFEResponse.Tender =
    CreateFEResponse.Tender(
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
        documents = this.documents.map { it.convert() },
        procurementMethodModalities = this.procurementMethodModalities,
        mainProcurementCategory = this.mainProcurementCategory,
        value = this.value,
        secondStage = this.secondStage?.convert(),
        otherCriteria = this.otherCriteria.convert(),
        contractPeriod = this.contractPeriod.convert(),
        criteria = this.criteria.map { it.convert() }
    )

fun CreateFEResult.Tender.Classification.convert(): CreateFEResponse.Tender.Classification =
    CreateFEResponse.Tender.Classification(
        scheme = this.scheme,
        id = this.id,
        description = this.description
    )

fun CreateFEResult.Tender.AcceleratedProcedure.convert(): CreateFEResponse.Tender.AcceleratedProcedure =
    CreateFEResponse.Tender.AcceleratedProcedure(
        isAcceleratedProcedure = this.isAcceleratedProcedure
    )

fun CreateFEResult.Tender.DesignContest.convert(): CreateFEResponse.Tender.DesignContest =
    CreateFEResponse.Tender.DesignContest(
        serviceContractAward = this.serviceContractAward
    )

fun CreateFEResult.Tender.ElectronicWorkflows.convert(): CreateFEResponse.Tender.ElectronicWorkflows =
    CreateFEResponse.Tender.ElectronicWorkflows(
        useOrdering = this.useOrdering,
        usePayment = this.usePayment,
        acceptInvoicing = this.acceptInvoicing
    )

fun CreateFEResult.Tender.JointProcurement.convert(): CreateFEResponse.Tender.JointProcurement =
    CreateFEResponse.Tender.JointProcurement(
        isJointProcurement = this.isJointProcurement
    )

fun CreateFEResult.Tender.ProcedureOutsourcing.convert(): CreateFEResponse.Tender.ProcedureOutsourcing =
    CreateFEResponse.Tender.ProcedureOutsourcing(
        procedureOutsourced = this.procedureOutsourced
    )

fun CreateFEResult.Tender.Framework.convert(): CreateFEResponse.Tender.Framework =
    CreateFEResponse.Tender.Framework(
        isAFramework = this.isAFramework
    )

fun CreateFEResult.Tender.DynamicPurchasingSystem.convert(): CreateFEResponse.Tender.DynamicPurchasingSystem =
    CreateFEResponse.Tender.DynamicPurchasingSystem(
        hasDynamicPurchasingSystem = this.hasDynamicPurchasingSystem
    )

fun CreateFEResult.Tender.Document.convert(): CreateFEResponse.Tender.Document =
    CreateFEResponse.Tender.Document(
        documentType = this.documentType,
        id = this.id,
        title = this.title,
        description = this.description
    )

fun CreateFEResult.Tender.SecondStage.convert(): CreateFEResponse.Tender.SecondStage =
    CreateFEResponse.Tender.SecondStage(
        minimumCandidates = this.minimumCandidates,
        maximumCandidates = this.maximumCandidates
    )

fun CreateFEResult.Tender.OtherCriteria.convert(): CreateFEResponse.Tender.OtherCriteria =
    CreateFEResponse.Tender.OtherCriteria(
        reductionCriteria = this.reductionCriteria,
        qualificationSystemMethods = this.qualificationSystemMethods
    )

fun CreateFEResult.Tender.ContractPeriod.convert(): CreateFEResponse.Tender.ContractPeriod =
    CreateFEResponse.Tender.ContractPeriod(
        startDate = this.startDate,
        endDate = this.endDate
    )

fun CreateFEResult.Tender.Criteria.convert(): CreateFEResponse.Tender.Criteria =
    CreateFEResponse.Tender.Criteria(
        id = this.id,
        title = this.title,
        description = this.description,
        relatesTo = this.relatesTo,
        source = this.source,
        requirementGroups = this.requirementGroups.map { it.convert() }
    )

fun CreateFEResult.Tender.Criteria.RequirementGroup.convert(): CreateFEResponse.Tender.Criteria.RequirementGroup =
    CreateFEResponse.Tender.Criteria.RequirementGroup(
        id = this.id,
        description = this.description,
        requirements = this.requirements
    )

fun CreateFEResult.Tender.ProcuringEntity.convert(): CreateFEResponse.Tender.ProcuringEntity =
    CreateFEResponse.Tender.ProcuringEntity(
        id = this.id,
        name = this.name,
        identifier = this.identifier.convert(),
        additionalIdentifiers = this.additionalIdentifiers.map { it.convert() },
        address = this.address.convert(),
        contactPoint = this.contactPoint.convert(),
        persons = this.persons.map { it.convert() }
    )

fun CreateFEResult.Tender.ProcuringEntity.Identifier.convert(): CreateFEResponse.Tender.ProcuringEntity.Identifier =
    CreateFEResponse.Tender.ProcuringEntity.Identifier(
        scheme = this.scheme,
        id = this.id,
        legalName = this.legalName,
        uri = this.uri
    )

fun CreateFEResult.Tender.ProcuringEntity.Address.convert(): CreateFEResponse.Tender.ProcuringEntity.Address =
    CreateFEResponse.Tender.ProcuringEntity.Address(
        streetAddress = this.streetAddress,
        postalCode = this.postalCode,
        addressDetails = this.addressDetails.convert()
    )

fun CreateFEResult.Tender.ProcuringEntity.Address.AddressDetails.convert(): CreateFEResponse.Tender.ProcuringEntity.Address.AddressDetails =
    CreateFEResponse.Tender.ProcuringEntity.Address.AddressDetails(
        country = this.country.convert(),
        region = this.region.convert(),
        locality = this.locality.convert()
    )

fun CreateFEResult.Tender.ProcuringEntity.Address.AddressDetails.Country.convert(): CreateFEResponse.Tender.ProcuringEntity.Address.AddressDetails.Country =
    CreateFEResponse.Tender.ProcuringEntity.Address.AddressDetails.Country(
        scheme = this.scheme,
        id = this.id,
        description = this.description,
        uri = this.uri
    )

fun CreateFEResult.Tender.ProcuringEntity.Address.AddressDetails.Region.convert(): CreateFEResponse.Tender.ProcuringEntity.Address.AddressDetails.Region =
    CreateFEResponse.Tender.ProcuringEntity.Address.AddressDetails.Region(
        scheme = this.scheme,
        id = this.id,
        description = this.description,
        uri = this.uri
    )

fun CreateFEResult.Tender.ProcuringEntity.Address.AddressDetails.Locality.convert(): CreateFEResponse.Tender.ProcuringEntity.Address.AddressDetails.Locality =
    CreateFEResponse.Tender.ProcuringEntity.Address.AddressDetails.Locality(
        scheme = this.scheme,
        id = this.id,
        description = this.description,
        uri = this.uri
    )

fun CreateFEResult.Tender.ProcuringEntity.ContactPoint.convert(): CreateFEResponse.Tender.ProcuringEntity.ContactPoint =
    CreateFEResponse.Tender.ProcuringEntity.ContactPoint(
        name = this.name,
        email = this.email,
        telephone = this.telephone,
        faxNumber = this.faxNumber,
        url = this.url
    )

fun CreateFEResult.Tender.ProcuringEntity.Person.convert(): CreateFEResponse.Tender.ProcuringEntity.Person =
    CreateFEResponse.Tender.ProcuringEntity.Person(
        id = this.id,
        title = this.title,
        name = this.name,
        identifier = this.identifier.convert(),
        businessFunctions = this.businessFunctions.map { it.convert() }
    )

fun CreateFEResult.Tender.ProcuringEntity.Person.Identifier.convert(): CreateFEResponse.Tender.ProcuringEntity.Person.Identifier =
    CreateFEResponse.Tender.ProcuringEntity.Person.Identifier(
        id = this.id,
        scheme = this.scheme,
        uri = this.uri
    )

fun CreateFEResult.Tender.ProcuringEntity.Person.BusinessFunction.convert(): CreateFEResponse.Tender.ProcuringEntity.Person.BusinessFunction =
    CreateFEResponse.Tender.ProcuringEntity.Person.BusinessFunction(
        id = this.id,
        jobTitle = this.jobTitle,
        type = this.type,
        period = this.period.convert(),
        documents = this.documents.map { it.convert() }
    )

fun CreateFEResult.Tender.ProcuringEntity.Person.BusinessFunction.Document.convert(): CreateFEResponse.Tender.ProcuringEntity.Person.BusinessFunction.Document =
    CreateFEResponse.Tender.ProcuringEntity.Person.BusinessFunction.Document(
        id = this.id,
        documentType = this.documentType,
        title = this.title,
        description = this.description
    )

fun CreateFEResult.Tender.ProcuringEntity.Person.BusinessFunction.Period.convert(): CreateFEResponse.Tender.ProcuringEntity.Person.BusinessFunction.Period =
    CreateFEResponse.Tender.ProcuringEntity.Person.BusinessFunction.Period(
        startDate = this.startDate
    )