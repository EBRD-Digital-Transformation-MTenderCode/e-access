package com.ocds.etender.model.dto.tender;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
public class Tender {
    public String id;
    public String title;
    public String description;
    public String status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Item> items;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Value minValue;//todo not find in file doc

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Value value;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String procurementMethod;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String procurementMethodDetails;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String procurementMethodAdditionalInfo;//todo not find in file doc

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String procurementMethodRationale;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String procurementCategory;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String awardCriteria;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String awardCriteriaDetails;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<String> submissionMethod;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String submissionMethodDetails;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Period tenderPeriod;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Period enquiryPeriod;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean hasEnquiries;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String eligibilityCriteria;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Period awardPeriod;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Period contractPeriod;//todo not find in file doc

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer numberOfTenderers;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<OrganizationReference> tenderers;//todo not find in file doc

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public OrganizationReference procuringEntity;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Document> documents;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Milestone> milestones;//todo not find in file doc

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Amendment> amendments;//todo not find in file doc

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Lot> lots;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public LotDetails lotDetails;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<LotGroup> lotGroups;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<ParticipationFee> participationFees;//todo not find in file doc

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Criterion criteria;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public AcceleratedProcedure acceleratedProcedure;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Classification classification;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public DesignContest designContest;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ElectronicWorkFlows electronicWorkflows;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public JointProcurement jointProcurement;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String legalBasis;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Objectives objectives;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ProcedureOutsourcing procedureOutsourcing;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<OrganizationReference> reviewParties;//todo not find in file doc

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Period reviewPeriod;//todo not find in file doc

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Period standstillPeriod;//todo not find in file doc

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<String> submissionLanguages;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<String> submissionMethodRationale;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public DynamicPurchasingSystem dynamicPurchasingSystem;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Framework framework;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean requiresElectronicCatalogue;//todo not find in file doc
}

