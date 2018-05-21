package com.procurement.access.service;

import com.procurement.access.model.dto.ocds.*;
import com.procurement.access.model.entity.TenderProcessEntity;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class StageServiceImpl implements StageService {

    private final JsonUtil jsonUtil;
    private final DateUtil dateUtil;
    private final TenderProcessDao tenderProcessDao;

    public StageServiceImpl(final JsonUtil jsonUtil, final DateUtil dateUtil, final TenderProcessDao tenderProcessDao) {
        this.jsonUtil = jsonUtil;
        this.dateUtil = dateUtil;
        this.tenderProcessDao = tenderProcessDao;
    }

    @Override
    public ResponseDto startNewStage(final String cpId,
                                     final String token,
                                     final String previousStage,
                                     final String newStage,
                                     final String owner) {

        final TenderProcessEntity entity = Optional.ofNullable(
                tenderProcessDao.getByCpIdAndStage(cpId, previousStage))
                .orElseThrow(() -> new ErrorException(ErrorType.DATA_NOT_FOUND));
        if (!entity.getOwner().equals(owner))
            throw new ErrorException(ErrorType.INVALID_OWNER);
        if (!entity.getToken().toString().equals(token))
            throw new ErrorException(ErrorType.INVALID_TOKEN);
        final TenderProcess processBefore = jsonUtil.toObject(TenderProcess.class, entity.getJsonData());
        if (processBefore.getTender().getStatus() != TenderStatus.ACTIVE)
            throw new ErrorException(ErrorType.NOT_ACTIVE);
        if (processBefore.getTender().getStatusDetails() != TenderStatusDetails.EMPTY)
            throw new ErrorException(ErrorType.NOT_INTERMEDIATE);
        if (!isHaveActiveLots(processBefore.getTender().getLots()))
            throw new ErrorException(ErrorType.NO_ACTIVE_LOTS);
        final Tender tender = processBefore.getTender();
        tender.setLots(filterLots(tender.getLots()));
        if (processBefore.getTender().getItems() != null)
        tender.setItems(filterItems(processBefore.getTender().getItems(), tender.getLots()));
        if (processBefore.getTender().getDocuments() != null)
            tender.setDocuments(filterDocuments(processBefore.getTender().getDocuments(), tender.getLots()));
        final TenderProcess tenderAfter = new TenderProcess(
                null,
                entity.getCpId(),
                processBefore.getPlanning(),
                tender);
        entity.setStage(newStage);
        entity.setCreatedDate(dateUtil.nowDateTime());
        entity.setJsonData(jsonUtil.toJson(tenderAfter));
        tenderProcessDao.save(entity);
        tenderAfter.setToken(entity.getToken().toString());
        return new ResponseDto<>(true, null, tenderAfter);
    }

    private boolean isHaveActiveLots(final List<Lot> lots) {
        return lots.stream()
                .anyMatch(lot ->
                        lot.getStatus().equals(TenderStatus.ACTIVE)
                                && lot.getStatusDetails().equals(TenderStatusDetails.EMPTY));
    }

    private List<Lot> filterLots(final List<Lot> lots) {
        return lots.stream()
                .filter(lot -> lot.getStatus().equals(TenderStatus.ACTIVE)
                        && lot.getStatusDetails().equals(TenderStatusDetails.EMPTY))
                .collect(Collectors.toList());
    }

    private Set<Item> filterItems(final Set<Item> items, final List<Lot> lots) {
        final Set<String> lotsID = getUniqueLots(lots);
        return items.stream().filter(item -> lotsID.contains(item.getRelatedLot())).collect(Collectors.toSet());
    }

    private List<Document> filterDocuments(final List<Document> documents, final List<Lot> lots) {
        final Set<Document> documentsAfterFilter = new HashSet<>();
        final Set<String> lotsID = getUniqueLots(lots);
        for (final Document document : documents) {
            if (document.getRelatedLots() == null || document.getRelatedLots().isEmpty()) {
                documentsAfterFilter.add(document);
            } else {
                if (document.getRelatedLots().stream().anyMatch(lotsID::contains))
                    documentsAfterFilter.add(document);
            }
        }
        return new ArrayList<>(documentsAfterFilter);
    }

    private Set<String> getUniqueLots(final List<Lot> lots) {
        return lots.stream().map(Lot::getId).collect(Collectors.toSet());
    }
}
