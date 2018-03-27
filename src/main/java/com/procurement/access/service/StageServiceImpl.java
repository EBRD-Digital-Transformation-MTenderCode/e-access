package com.procurement.access.service;

import com.procurement.access.dao.TenderDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ocds.Document;
import com.procurement.access.model.dto.ocds.Item;
import com.procurement.access.model.dto.ocds.Lot;
import com.procurement.access.model.dto.ocds.Tender;
import com.procurement.access.model.dto.ocds.TenderStatus;
import com.procurement.access.model.dto.ocds.TenderStatusDetails;
import com.procurement.access.model.dto.tender.CnDto;
import com.procurement.access.model.entity.TenderEntity;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class StageServiceImpl implements StageService {

    private static final String DATA_NOT_FOUND_ERROR = "Data not found.";
    private static final String INVALID_OWNER_ERROR = "Invalid owner.";
    private static final String INVALID_STATUS_ERROR = "Status now is not Active.";
    private static final String INVALID_STATUS_DETAILS_ERROR = "Status Details now is not Empty.";
    private static final String INVALID_LOTS = "Not have a one valid lot.";
    private static final String NEW_STAGE = "new stage";

    private final JsonUtil jsonUtil;
    private final DateUtil dateUtil;
    private final TenderDao tenderDao;

    public StageServiceImpl(final JsonUtil jsonUtil, final DateUtil dateUtil, final TenderDao tenderDao) {
        this.jsonUtil = jsonUtil;
        this.dateUtil = dateUtil;
        this.tenderDao = tenderDao;
    }

    @Override
    public ResponseDto startNewStage(final String cpId,
                                     final String token,
                                     final String previousStage,
                                     final String stage,
                                     final String owner) {

        final TenderEntity entity = Optional.ofNullable(tenderDao.getByCpIdAndTokenAndStage(cpId, UUID.fromString
            (token), null))
                                            .orElseThrow(() -> new ErrorException(DATA_NOT_FOUND_ERROR));
        if (!entity.getOwner()
                   .equals(owner)) throw new ErrorException(INVALID_OWNER_ERROR);
        final CnDto tenderBefore = jsonUtil.toObject(CnDto.class, entity.getJsonData());
        if (tenderBefore.getTender()
                        .getStatus() != TenderStatus.ACTIVE) throw new ErrorException(INVALID_STATUS_ERROR);

        if (tenderBefore.getTender()
                        .getStatusDetails() != TenderStatusDetails.EMPTY)
            throw new ErrorException(INVALID_STATUS_DETAILS_ERROR);
        if (!isHaveActiveLots(tenderBefore.getTender()
                                          .getLots())) throw new ErrorException(INVALID_LOTS);

        Tender tender = tenderBefore.getTender();

        tender.setLots(filterLots(tenderBefore.getTender()
                                              .getLots()));
        tender.setItems(filterItems(tenderBefore.getTender()
                                                .getItems(), tender.getLots()));
        tender.setDocuments(filterDocuments(tenderBefore.getTender()
                                                        .getDocuments(), tender.getLots()));

        final CnDto tenderAfter = new CnDto(entity.getToken()
                                                  .toString(), entity.getCpId(), tenderBefore.getPlanning(), tender);

        entity.setJsonData(jsonUtil.toJson(tenderAfter));
        entity.setStage(NEW_STAGE);

        tenderDao.save(entity);
        tenderAfter.setToken(entity.getToken()
                                   .toString());
        return new ResponseDto<>(true, null, tenderAfter);
    }

    private boolean isHaveActiveLots(List<Lot> lots) {
        for (int i = 0; i < lots.size(); i++) {
            Lot lot = lots.get(i);
            if (lot.getStatus() == TenderStatus.ACTIVE && lot.getStatusDetails() == TenderStatusDetails.EMPTY) {
                return true;
            }
        }
        return false;
    }

    private List<Lot> filterLots(List<Lot> lots) {
        List<Lot> lotsAfterFilter = new ArrayList<>();
        for (int i = 0; i < lots.size(); i++) {
            Lot lot = lots.get(i);
            if (lot.getStatus() == TenderStatus.ACTIVE && lot.getStatusDetails() == TenderStatusDetails.EMPTY) {
                lotsAfterFilter.add(lot);
            }
        }
        return lotsAfterFilter;
    }

    private Set<Item> filterItems(Set<Item> items, List<Lot> lots) {
        Set<Item> itemsAfterFilter = new HashSet<>();
        for (Item item : items) {
            for (int i = 0; i < lots.size(); i++) {
                if (item.getRelatedLot()
                        .equals(lots.get(i)
                                    .getId())) {
                    itemsAfterFilter.add(item);
                }
            }
        }
        return itemsAfterFilter;
    }

    private List<Document> filterDocuments(List<Document> documents, List<Lot> lots) {
        List<Document> documentsAfterFilter = new ArrayList<>();
        for (Document document : documents) {
            if (document.getRelatedLots()
                        .size() == 0) {
                documentsAfterFilter.add(document);
            } else {
                List<String> relatedLots = document.getRelatedLots();
                for (int i = 0; i < relatedLots.size(); i++) {
                    for (int j = 0; j < lots.size(); j++) {
                        if (relatedLots.get(i)
                                       .equals(lots.get(j)
                                                   .getId())) {
                            documentsAfterFilter.add(document);
                        }
                    }
                }
            }
        }
        return documentsAfterFilter;
    }
}
