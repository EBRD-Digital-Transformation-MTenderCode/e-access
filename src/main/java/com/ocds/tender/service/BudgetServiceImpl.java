package com.ocds.tender.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocds.tender.model.dto.budget.Budget;
import com.ocds.tender.model.entity.BudgetEntity;
import com.ocds.tender.repository.BudgetRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
public class BudgetServiceImpl implements BudgetService {

    private BudgetRepository budgetRepository;

    private ObjectMapper objectMapper;

    public BudgetServiceImpl(BudgetRepository budgetRepository,
                             ObjectMapper objectMapper) {
        this.budgetRepository = budgetRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void updateData(String ocId, Date addedDate, Budget budgetDto) {
        if (Objects.nonNull(budgetDto)) {
            BudgetEntity budgetEntity = new BudgetEntity();
            budgetEntity.setOcId(ocId);
            budgetEntity.setAddedDate(addedDate);
            try {
                String budgetJson = objectMapper.writeValueAsString(budgetDto);
                budgetEntity.setJsonData(budgetJson);
                budgetRepository.save(budgetEntity);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
}
