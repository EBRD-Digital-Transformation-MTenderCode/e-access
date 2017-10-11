package com.ocds.tender.model.dto.tender;

import lombok.Data;

import java.util.List;

@Data
public class DesignContest {
    public Boolean hasPrizes;
    public List<Item> prizes;
    public String paymentsToParticipants;
    public Boolean serviceContractAward;
    public Boolean juryDecisionBinding;
    public List<OrganizationReference> juryMembers;
    public List<OrganizationReference> participants;
}
