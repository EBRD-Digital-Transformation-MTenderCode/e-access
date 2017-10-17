package com.ocds.access.service;

import com.ocds.access.model.dto.budget.Budget;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public interface BudgetService {

    void insertData(String ocId, Date addedDate, Budget data);

    void updateData(String ocId, Date addedDate, Budget data);
}
