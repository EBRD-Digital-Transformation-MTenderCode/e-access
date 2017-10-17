package com.ocds.access.service;

import com.ocds.access.model.dto.budget.Budget;
import com.ocds.access.model.entity.BudgetEntity;
import com.ocds.access.model.entity.EventType;
import com.ocds.access.repository.BudgetRepository;
import com.ocds.access.utils.JsonUtil;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Service
public class BudgetServiceImpl implements BudgetService {

    private BudgetRepository budgetRepository;

    private JsonUtil jsonUtil;

    public BudgetServiceImpl(BudgetRepository budgetRepository,
                            JsonUtil jsonUtil) {
        this.budgetRepository = budgetRepository;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public void insertData(String ocId, Date addedDate, Budget budgetDto) {
        Objects.requireNonNull(ocId);
        Objects.requireNonNull(addedDate);
        Objects.requireNonNull(budgetDto);
        convertDtoToEntity(ocId, addedDate, budgetDto)
            .ifPresent(budgetRepository::save);
    }

    @Override
    public void updateData(String ocId, Date addedDate, Budget budgetDto) {
        Objects.requireNonNull(ocId);
        Objects.requireNonNull(addedDate);
        Objects.requireNonNull(budgetDto);
        BudgetEntity sourceBudgetEntity = budgetRepository.getLastByOcId(ocId);
        Budget mergedBudget = mergeJson(sourceBudgetEntity, budgetDto);
        convertDtoToEntity(ocId, addedDate, mergedBudget)
            .ifPresent(budgetRepository::save);
    }

    public Budget mergeJson(BudgetEntity budgetEntity, Budget budgetDto) {
        Objects.requireNonNull(budgetEntity);
        Objects.requireNonNull(budgetDto);
        String sourceJson = budgetEntity.getJsonData();
        String updateJson = jsonUtil.toJson(budgetDto);
        String mergedJson = jsonUtil.merge(sourceJson, updateJson);
        return jsonUtil.toObject(Budget.class, mergedJson);
    }

    public Optional<BudgetEntity> convertDtoToEntity(String ocId, Date addedDate, Budget budgetDto) {
        String budgetJson = jsonUtil.toJson(budgetDto);
        if (!budgetJson.equals("{}")) {
            BudgetEntity budgetEntity = new BudgetEntity();
            budgetEntity.setOcId(ocId);
            budgetEntity.setAddedDate(addedDate);
            budgetEntity.setEventType(EventType.BUDGET.getText());
            budgetEntity.setJsonData(budgetJson);
            return Optional.of(budgetEntity);
        } else {
            return Optional.empty();
        }
    }
}
