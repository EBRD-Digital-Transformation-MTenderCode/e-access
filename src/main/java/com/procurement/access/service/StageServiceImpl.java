package com.procurement.access.service;

import com.procurement.access.dao.TenderProcessDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ocds.*;
import com.procurement.access.model.dto.tender.TenderProcessDto;
import com.procurement.access.model.entity.TenderProcessEntity;
import com.procurement.access.utils.JsonUtil;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class StageServiceImpl implements StageService {

    private static final String DATA_NOT_FOUND_ERROR = "Data not found.";
    private static final String INVALID_OWNER_ERROR = "Invalid owner.";
    private static final String INVALID_STATUS_ERROR = "Status now is not Active.";
    private static final String INVALID_STATUS_DETAILS_ERROR = "Status Details now is not Empty.";
    private static final String INVALID_LOTS = "Not have a one valid lot.";

    private final JsonUtil jsonUtil;
    private final TenderProcessDao tenderProcessDao;

    public StageServiceImpl(final JsonUtil jsonUtil, final TenderProcessDao tenderProcessDao) {
        this.jsonUtil = jsonUtil;
        this.tenderProcessDao = tenderProcessDao;
    }

    @Override
    public ResponseDto startNewStage(final String cpId,
                                     final String token,
                                     final String previousStage,
                                     final String newStage,
                                     final String owner) {

        final TenderProcessEntity entity = Optional.ofNullable(
                tenderProcessDao.getByCpIdAndTokenAndStage(cpId, UUID.fromString(token), previousStage))
                .orElseThrow(() -> new ErrorException(DATA_NOT_FOUND_ERROR));
        if (!entity.getOwner().equals(owner)) throw new ErrorException(INVALID_OWNER_ERROR);
        final TenderProcessDto processBefore = jsonUtil.toObject(TenderProcessDto.class, entity.getJsonData());
        if (processBefore.getTender().getStatus() != TenderStatus.ACTIVE)
            throw new ErrorException(INVALID_STATUS_ERROR);
        if (processBefore.getTender().getStatusDetails() != TenderStatusDetails.EMPTY)
            throw new ErrorException(INVALID_STATUS_DETAILS_ERROR);
        if (!isHaveActiveLots(processBefore.getTender().getLots()))
            throw new ErrorException(INVALID_LOTS);
        Tender tender = processBefore.getTender();
        tender.setLots(filterLots(tender.getLots()));
        tender.setItems(filterItems(processBefore.getTender().getItems(), tender.getLots()));
        tender.setDocuments(filterDocuments(processBefore.getTender().getDocuments(), tender.getLots()));
        final TenderProcessDto tenderAfter = new TenderProcessDto(
                entity.getToken().toString(),
                entity.getCpId(),
                processBefore.getPlanning(),
                tender);
        entity.setStage(newStage);
        entity.setJsonData(jsonUtil.toJson(tenderAfter));
        tenderProcessDao.save(entity);
        tenderAfter.setToken(entity.getToken().toString());
        return new ResponseDto<>(true, null, tenderAfter);
    }

    private boolean isHaveActiveLots(List<Lot> lots) {
        return lots.stream()
                .anyMatch(lot ->
                        (lot.getStatus().equals(TenderStatus.ACTIVE)
                                && lot.getStatusDetails().equals(TenderStatusDetails.EMPTY)));
    }

    private List<Lot> filterLots(List<Lot> lots) {
        return lots.stream()
                .filter(lot -> (lot.getStatus().equals(TenderStatus.ACTIVE)
                        && lot.getStatusDetails().equals(TenderStatusDetails.EMPTY)))
                .collect(Collectors.toList());
    }

    private Set<Item> filterItems(Set<Item> items, List<Lot> lots) {
        Set<String> lotsID = lots.stream().map(Lot::getId).collect(Collectors.toSet());
        return items.stream().filter(item -> lotsID.contains(item.getRelatedLot())).collect(Collectors.toSet());
    }

    private List<Document> filterDocuments(List<Document> documents, List<Lot> lots) {
        Set<Document> documentsAfterFilter = new HashSet<>();
        Set<String> lotsID = lots.stream().map(Lot::getId).collect(Collectors.toSet());
        for (Document document : documents) {
            if (document.getRelatedLots().size() == 0) {
                documentsAfterFilter.add(document);
            } else {
                if (document.getRelatedLots().stream().anyMatch(lotsID::contains))
                    documentsAfterFilter.add(document);
            }
        }
        return new ArrayList<>(documentsAfterFilter);
    }
}
