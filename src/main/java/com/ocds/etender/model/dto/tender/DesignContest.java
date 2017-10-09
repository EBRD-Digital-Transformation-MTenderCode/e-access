package com.ocds.etender.model.dto.tender;

import java.util.List;

public class DesignContest {
    public Boolean hasPrizes;
    public List<Item> prizes;
    public String paymentsToParticipants;
    public Boolean serviceContractAward;
    public Boolean juryDecisionBinding;
    public List<OrganizationReference> juryMembers;
    public List<OrganizationReference> participants;
}
