package com.ocds.tender.model.dto.tender;

import lombok.Data;

import java.util.List;

@Data
public class Tender {
    public String id;
    public String title;
    public String description;
    public String status;
    public List<Item> items;
    public Value minValue;//todo not find in file doc
    public Value value;
    public String procurementMethod;
    public String procurementMethodDetails;
    public String procurementMethodAdditionalInfo;//todo not find in file doc
    public String procurementMethodRationale;
    public String procurementCategory;
    public String awardCriteria;
    public String awardCriteriaDetails;
    public List<String> submissionMethod;
    public String submissionMethodDetails;
    public Period tenderPeriod;
    public Period enquiryPeriod;
    public Boolean hasEnquiries;
    public String eligibilityCriteria;
    public Period awardPeriod;
    public Period contractPeriod;//todo not find in file doc
    public Integer numberOfTenderers;
    public List<OrganizationReference> tenderers;//todo not find in file doc
    public OrganizationReference procuringEntity;
    public List<Document> documents;
    public List<Milestone> milestones;//todo not find in file doc
    public List<Amendment> amendments;//todo not find in file doc
    public List<Lot> lots;
    public LotDetails lotDetails;
    public List<LotGroup> lotGroups;
    public List<ParticipationFee> participationFees;//todo not find in file doc
    public Criterion criteria;
    public AcceleratedProcedure acceleratedProcedure;
    public Classification classification;
    public DesignContest designContest;
    public ElectronicWorkFlows electronicWorkflows;
    public JointProcurement jointProcurement;
    public String legalBasis;
    public Objectives objectives;
    public ProcedureOutsourcing procedureOutsourcing;
    public List<OrganizationReference> reviewParties;//todo not find in file doc
    public Period reviewPeriod;//todo not find in file doc
    public Period standstillPeriod;//todo not find in file doc
    public List<String> submissionLanguages;
    public List<String> submissionMethodRationale;
    public DynamicPurchasingSystem dynamicPurchasingSystem;
    public Framework framework;
    public Boolean requiresElectronicCatalogue;//todo not find in file doc
}

