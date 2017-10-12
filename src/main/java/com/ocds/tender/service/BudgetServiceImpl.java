package com.ocds.tender.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocds.tender.model.dto.budget.Budget;
import com.ocds.tender.model.entity.BudgetEntity;
import com.ocds.tender.model.entity.EventType;
import com.ocds.tender.repository.BudgetRepository;
import com.ocds.tender.utils.JsonUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
    public void insertData(String ocId, Date addedDate, Budget budgetDto) {
        if (Objects.nonNull(budgetDto)) {
            BudgetEntity budgetEntity = convertDtoToEntity(ocId, addedDate, budgetDto);
            saveEntity(budgetEntity);
        }
    }

    @Override
    public void updateData(String ocId, Date addedDate, Budget budgetDto) {
        if (Objects.nonNull(budgetDto)) {
            BudgetEntity sourceBudgetEntity = budgetRepository.getLastByOcId(ocId);
            if (Objects.nonNull(sourceBudgetEntity)) {
                Budget newBudgetDto = mergeData(sourceBudgetEntity.getJsonData(), budgetDto);
                if (Objects.nonNull(newBudgetDto)) {
                    BudgetEntity newBudgetEntity = convertDtoToEntity(ocId, addedDate, newBudgetDto);
                    saveEntity(newBudgetEntity);
                }
            } else {
                BudgetEntity budgetEntity = convertDtoToEntity(ocId, addedDate, budgetDto);
                saveEntity(budgetEntity);
            }
        }
    }

    public Budget mergeData(String sourceJsonData, Budget budgetDto) {
        JsonNode updateJson = objectMapper.valueToTree(budgetDto);
        JsonNode sourceJson = null;
        try {
            sourceJson = objectMapper.readTree(sourceJsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonNode mergedJson = null;
        if (Objects.nonNull(sourceJson) && Objects.nonNull(updateJson)) {
            mergedJson = JsonUtils.merge(sourceJson, updateJson);
        }
        Budget budget = null;
        if (Objects.nonNull(mergedJson)) {
            try {
                budget = objectMapper.treeToValue(mergedJson, Budget.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return budget;
    }

    public BudgetEntity convertDtoToEntity(String ocId, Date addedDate, Budget budgetDto) {
        BudgetEntity budgetEntity = null;
        if (Objects.nonNull(budgetDto)) {
            budgetEntity = new BudgetEntity();
            budgetEntity.setOcId(ocId);
            budgetEntity.setAddedDate(addedDate);
            try {
                String budgetJson = objectMapper.writeValueAsString(budgetDto);
                budgetEntity.setJsonData(budgetJson);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            budgetEntity.setEventType(EventType.BUDGET.getText());
        }
        return budgetEntity;
    }

    public void saveEntity(BudgetEntity budgetEntity) {
        if (Objects.nonNull(budgetEntity.getJsonData())) {
            budgetRepository.save(budgetEntity);
        }
    }
}
