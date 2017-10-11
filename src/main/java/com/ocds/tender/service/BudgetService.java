package com.ocds.tender.service;

import com.ocds.tender.model.dto.budget.Budget;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public interface BudgetService {

    void updateData(String ocId, Date addedDate,  Budget data);

}
