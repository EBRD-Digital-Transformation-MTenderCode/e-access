package com.procurement.access.service;

import com.datastax.driver.core.utils.UUIDs;
import com.procurement.access.config.properties.OCDSProperties;
import com.procurement.access.dao.TenderProcessDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.exception.ErrorType;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ocds.OrganizationReference;
import com.procurement.access.model.dto.pn.PnProcess;
import com.procurement.access.model.dto.pn.PnLot;
import com.procurement.access.model.dto.pn.PnTender;
import com.procurement.access.model.entity.TenderProcessEntity;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;

import static com.procurement.access.model.dto.ocds.TenderStatus.PLANNING;
import static com.procurement.access.model.dto.ocds.TenderStatusDetails.EMPTY;

@Service
public class PNServiceImpl implements PNService {

    private static final String SEPARATOR = "-";
    private final OCDSProperties ocdsProperties;
    private final JsonUtil jsonUtil;
    private final DateUtil dateUtil;
    private final TenderProcessDao tenderProcessDao;

    public PNServiceImpl(final OCDSProperties ocdsProperties,
                         final JsonUtil jsonUtil,
                         final DateUtil dateUtil,
                         final TenderProcessDao tenderProcessDao) {
        this.ocdsProperties = ocdsProperties;
        this.jsonUtil = jsonUtil;
        this.dateUtil = dateUtil;
        this.tenderProcessDao = tenderProcessDao;
    }

    @Override
    public ResponseDto createPn(final String stage,
                                final String country,
                                final String owner,
                                final LocalDateTime dateTime,
                                final PnProcess dto) {
        validateFields(dto);
        final PnTender tender = dto.getTender();
        final String cpId = getCpId(country);
        tender.setId(cpId);
        dto.setOcId(cpId);
        setLotsStatus(tender);
        setTenderStatus(tender);
        setItemsId(tender);
        setLotsIdAndItemsAndDocumentsRelatedLots(tender);
        setIdOfOrganizationReference(tender.getProcuringEntity());
        final TenderProcessEntity entity = getEntity(dto, stage, dateTime, owner);
        tenderProcessDao.save(entity);
        dto.setToken(entity.getToken().toString());
        return new ResponseDto<>(true, null, dto);
    }

    private void validateFields(final PnProcess dto) {
        if (Objects.nonNull(dto.getTender().getId())) throw new ErrorException(ErrorType.TENDER_ID_NOT_NULL);
        if (Objects.nonNull(dto.getTender().getStatus())) throw new ErrorException(ErrorType.TENDER_STATUS_NOT_NULL);
        if (Objects.nonNull(dto.getTender().getStatusDetails()))
            throw new ErrorException(ErrorType.TENDER_STATUS_DETAILS_NOT_NULL);
        if (dto.getTender().getLots().stream().anyMatch(l -> Objects.nonNull(l.getStatus())))
            throw new ErrorException(ErrorType.LOT_STATUS_NOT_NULL);
        if (dto.getTender().getLots().stream().anyMatch(l -> Objects.nonNull(l.getStatusDetails())))
            throw new ErrorException(ErrorType.LOT_STATUS_DETAILS_NOT_NULL);
    }

    private String getCpId(final String country) {
        return ocdsProperties.getPrefix() + SEPARATOR + country + SEPARATOR + dateUtil.milliNowUTC();
    }

    private void setIdOfOrganizationReference(final OrganizationReference or) {
        or.setId(or.getIdentifier().getScheme() + SEPARATOR + or.getIdentifier().getId());
    }

    private void setTenderStatus(final PnTender tender) {
        tender.setStatus(PLANNING);
        tender.setStatusDetails(EMPTY);
    }

    private void setLotsStatus(final PnTender tender) {
        tender.getLots().forEach(lot -> {
            lot.setStatus(PLANNING);
            lot.setStatusDetails(EMPTY);
        });
    }

    private void setItemsId(final PnTender tender) {
        tender.getItems().forEach(item -> item.setId(UUIDs.timeBased().toString()));
    }

    private void setLotsIdAndItemsAndDocumentsRelatedLots(final PnTender tender) {
        for (final PnLot lot : tender.getLots()) {
            final String id = UUIDs.timeBased().toString();
            if (Objects.nonNull(tender.getItems())) {
                tender.getItems()
                        .stream()
                        .filter(item -> item.getRelatedLot().equals(lot.getId()))
                        .forEach(item -> item.setRelatedLot(id));
            }
            if (Objects.nonNull(tender.getDocuments())) {
                tender.getDocuments().forEach(document -> {
                    final Set<String> relatedLots = document.getRelatedLots();
                    if (relatedLots.contains(lot.getId())) {
                        relatedLots.remove(lot.getId());
                        relatedLots.add(id);
                    }
                    document.setRelatedLots(relatedLots);
                });
            }
            lot.setId(id);
        }
    }

    private TenderProcessEntity getEntity(final PnProcess dto,
                                          final String stage,
                                          final LocalDateTime dateTime,
                                          final String owner) {
        final TenderProcessEntity entity = new TenderProcessEntity();
        entity.setCpId(dto.getTender().getId());
        entity.setToken(UUIDs.random());
        entity.setOwner(owner);
        entity.setStage(stage);
        entity.setCreatedDate(dateUtil.localToDate(dateTime));
        entity.setCreatedDate(dateUtil.localToDate(LocalDateTime.now()));
        entity.setJsonData(jsonUtil.toJson(dto));
        return entity;
    }
}
