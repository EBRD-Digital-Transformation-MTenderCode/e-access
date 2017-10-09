package com.ocds.etender.model.dto.tender;

import java.util.List;

/**
 * Author: user
 * Created by: ModelGenerator on 10/5/17
 */
public class Tender {
    public String id;
    public String title;
    public String description;
    public String procurementCategory;
    public String procurementMethod;
    public String procurementMethodDetails;
    public String procurmentMethodAdditionalInfo;//todo added from file doc
    public String procurementMethodRationale;
    public AcceleratedProcedure acceleratedProcedure;
    public Framework framework;
    public DynamicPurchasingSystem dynamicPurchasingSystem;
    public JointProcurement jointProcurement;
    public String status;
    public Classification classification;
    public Boolean hasEnquiries;
    public Period enquiryPeriod;
    public Value value;
    public String legalBasis;
    public ProcedureOutsourcing procedureOutsourcing;
    public Objectives objectives;
    public DesignContest designContest;
    public LotDetails lotDetails;
    public List<Lot> lots;
    public List<LotGroup> lotGroups;
    public List<Item> items;
    public String awardCriteria;
    public String awardCriteriaDetails;
    public Period awardPeriod;
    public Criterion criteria;
    public Integer numberOfTenderers;
    public List<String> submissionMethod;
    public String submissionMethodDetails;
    public List<String> submissionMethodRationale;
    public List<String> submissionLanguages;
    public Period tenderPeriod;
    public String eligibilityCriteria;
    public ElectronicWorkflows electronicWorkflows;
    public List<Document> documents;
    public OrganizationReference procuringEntity;
    public Period contractPeriod;//todo not find in file doc
    public Period reviewPeriod;//todo not find in file doc
    public Period standstillPeriod;//todo not find in file doc
    public Value minValue;//todo not find in file doc
    public List<OrganizationReference> tenderers;//todo not find in file doc
    public List<Milestone> milestones;//todo not find in file doc
    public List<ParticipationFee> participationFees;//todo not find in file doc
    public List<OrganizationReference> reviewParties;//todo not find in file doc
    public String procurementMethodAdditionalInfo;//todo not find in file doc
    public List<Amendment> amendments;//todo not find in file doc
}

