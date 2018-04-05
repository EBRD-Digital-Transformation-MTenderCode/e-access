package com.procurement.access.service;

import com.datastax.driver.core.utils.UUIDs;
import com.procurement.access.config.properties.OCDSProperties;
import com.procurement.access.dao.TenderProcessDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.exception.ErrorType;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ocds.Lot;
import com.procurement.access.model.dto.ocds.OrganizationReference;
import com.procurement.access.model.dto.ocds.Tender;
import com.procurement.access.model.dto.tender.TenderProcessRequestDto;
import com.procurement.access.model.dto.tender.TenderProcessResponseDto;
import com.procurement.access.model.dto.tender.TenderRequest;
import com.procurement.access.model.entity.TenderProcessEntity;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

import static com.procurement.access.model.dto.ocds.TenderStatus.ACTIVE;
import static com.procurement.access.model.dto.ocds.TenderStatusDetails.EMPTY;

@Service
public class TenderProcessServiceImpl implements TenderProcessService {

    private static final String SEPARATOR = "-";
    private final OCDSProperties ocdsProperties;
    private final JsonUtil jsonUtil;
    private final DateUtil dateUtil;
    private final TenderProcessDao tenderProcessDao;

    public TenderProcessServiceImpl(final OCDSProperties ocdsProperties,
                                    final JsonUtil jsonUtil,
                                    final DateUtil dateUtil,
                                    final TenderProcessDao tenderProcessDao) {
        this.ocdsProperties = ocdsProperties;
        this.jsonUtil = jsonUtil;
        this.dateUtil = dateUtil;
        this.tenderProcessDao = tenderProcessDao;
    }

    @Override
    public ResponseDto createCn(final String stage,
                                final String country,
                                final String owner,
                                final LocalDateTime dateTime,
                                final TenderProcessRequestDto dto) {
        final String cpId = getCpId(country);
        final Tender tender = getTenderProcessResponseDto(dto);
        tender.setId(cpId);
        setLotsStatus(tender);
        setTenderStatus(tender);
        setItemsId(tender);
        setLotsIdAndItemsAndDocumentsRelatedLots(tender);
        setIdOfOrganizationReference(tender.getProcuringEntity());
        final TenderProcessEntity entity = getEntity(dto, stage, dateTime, owner);
        tenderProcessDao.save(entity);
        dto.setOcId(cpId);
        dto.setToken(entity.getToken().toString());
        return new ResponseDto<>(true, null, dto);
    }

    @Override
    public ResponseDto updateCn(final String cpId,
                                final String token,
                                final String owner,
                                final TenderProcessResponseDto cn) {
        final TenderProcessEntity entity = Optional.ofNullable(tenderProcessDao.getByCpIdAndToken(cpId, UUID.fromString(token)))
                .orElseThrow(() -> new ErrorException(ErrorType.DATA_NOT_FOUND));
        if (!entity.getOwner().equals(owner))
            throw new ErrorException(ErrorType.INVALID_OWNER);
        final TenderProcessResponseDto tender = jsonUtil.toObject(TenderProcessResponseDto.class, entity.getJsonData());
        tender.setTender(cn.getTender());
        entity.setJsonData(jsonUtil.toJson(tender));
        tenderProcessDao.save(entity);
        cn.setToken(entity.getToken().toString());
        return new ResponseDto<>(true, null, cn);
    }

    @Override
    public ResponseDto createPin(final String stage,
                                 final String country,
                                 final String owner,
                                 final LocalDateTime dateTime,
                                 final TenderProcessResponseDto dto) {
        return null;
    }

    private Tender getTenderProcessResponseDto(final TenderRequest tender) {

    }

    private String getCpId(final String country) {
        return ocdsProperties.getPrefix() + SEPARATOR + country + SEPARATOR + dateUtil.milliNowUTC();
    }

    private void setIdOfOrganizationReference(final OrganizationReference or) {
        or.setId(or.getIdentifier().getScheme() + SEPARATOR + or.getIdentifier().getId());
    }

    private void setTenderStatus(final Tender tender) {
        tender.setStatus(ACTIVE);
        tender.setStatusDetails(EMPTY);
    }

    private void setLotsStatus(final Tender tender) {
        tender.getLots().forEach(lot -> {
            lot.setStatus(ACTIVE);
            lot.setStatusDetails(EMPTY);
        });
    }

    private void setItemsId(final Tender tender) {
        tender.getItems().forEach(item -> item.setId(UUIDs.timeBased().toString()));
    }

    private void setLotsIdAndItemsAndDocumentsRelatedLots(final Tender tender) {
        for (final Lot lot : tender.getLots()) {
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
                    if (Objects.nonNull(relatedLots)) {
                        if (relatedLots.contains(lot.getId())) {
                            relatedLots.remove(lot.getId());
                            relatedLots.add(id);
                        }
                        document.setRelatedLots(relatedLots);
                    }
                });
            }
            lot.setId(id);
        }
    }

    private TenderProcessEntity getEntity(final TenderProcessResponseDto dto,
                                          final String stage,
                                          final LocalDateTime dateTime,
                                          final String owner) {
        final TenderProcessEntity entity = new TenderProcessEntity();
        entity.setCpId(dto.getTender().getId());
        entity.setToken(UUIDs.random());
        entity.setStage(stage);
        entity.setOwner(owner);
        entity.setCreatedDate(dateUtil.localToDate(dateTime));
        entity.setJsonData(jsonUtil.toJson(dto));
        return entity;
    }

}
