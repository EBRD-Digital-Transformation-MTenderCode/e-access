package com.procurement.access.service;

import com.datastax.driver.core.utils.UUIDs;
import com.procurement.access.config.properties.OCDSProperties;
import com.procurement.access.dao.TenderDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ocds.OrganizationReference;
import com.procurement.access.model.dto.ocds.Tender;
import com.procurement.access.model.dto.tender.TenderDto;
import com.procurement.access.model.dto.tender.TenderResponseDto;
import com.procurement.access.model.entity.TenderEntity;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

import static com.procurement.access.model.dto.ocds.TenderStatus.ACTIVE;
import static com.procurement.access.model.dto.ocds.TenderStatusDetails.EMPTY;

@Service
public class CnServiceImpl implements CnService {

    private static final String DATA_NOT_FOUND_ERROR = "Data not found.";
    private static final String INVALID_OWNER_ERROR = "Invalid owner.";
    private final OCDSProperties ocdsProperties;
    private final JsonUtil jsonUtil;
    private final DateUtil dateUtil;
    private final TenderDao tenderDao;

    public CnServiceImpl(final OCDSProperties ocdsProperties,
                         final JsonUtil jsonUtil,
                         final DateUtil dateUtil,
                         final TenderDao tenderDao) {
        this.ocdsProperties = ocdsProperties;
        this.jsonUtil = jsonUtil;
        this.dateUtil = dateUtil;
        this.tenderDao = tenderDao;
    }

    @Override
    public ResponseDto createCn(final String owner,
                                final LocalDateTime startDate,
                                final TenderDto tenderDto) {
        final String cpId = ocdsProperties.getPrefix() + dateUtil.getMilliNowUTC();
        tenderDto.setOcId(cpId);
        tenderDto.setDate(startDate);
        final Tender tender = tenderDto.getTender();
        setTenderId(tender, cpId);
        setItemsId(tender);
        setLotsIdAndItemsRelatedLots(tender);
        setTenderStatus(tender);
        setLotsStatus(tender);
        processBuyer(tenderDto.getBuyer());
        final TenderEntity entity = getEntity(tenderDto, owner);
        tenderDao.save(entity);
        return getResponseDto(entity.getCpId(), entity.getToken().toString(), tenderDto);
    }

    @Override
    public ResponseDto updateCn(final String owner,
                                final String cpId,
                                final String token,
                                final TenderDto tenderDto) {
        final TenderEntity entity = Optional.ofNullable(tenderDao.getByCpIdAndToken(cpId, UUID.fromString(token)))
                .orElseThrow(() -> new ErrorException(DATA_NOT_FOUND_ERROR));
        if (!entity.getOwner().equals(owner)) throw new ErrorException(INVALID_OWNER_ERROR);
        final TenderDto tender = jsonUtil.toObject(TenderDto.class, entity.getJsonData());
        tender.setDate(dateUtil.getNowUTC());
        tender.setTender(tenderDto.getTender());
        entity.setJsonData(jsonUtil.toJson(tender));
        tenderDao.save(entity);
        return getResponseDto(cpId, entity.getToken().toString(), tender);
    }

    private void setTenderId(final Tender tender, final String cpId) {
        tender.setId(cpId);
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

    private void setLotsIdAndItemsRelatedLots(final Tender tender) {
        tender.getLots().forEach(lot -> {
            final String id = UUIDs.timeBased().toString();
            tender.getItems()
                    .stream()
                    .filter(item -> item.getRelatedLot().equals(lot.getId()))
                    .findFirst()
                    .get()
                    .setRelatedLot(id);
            lot.setId(id);
        });
    }

    private void processBuyer(final OrganizationReference buyer) {
        buyer.setId(buyer.getIdentifier().getScheme() + "-" + buyer.getIdentifier().getId());
    }

    private TenderEntity getEntity(final TenderDto tender, final String owner) {
        final TenderEntity entity = new TenderEntity();
        entity.setCpId(tender.getTender().getId());
        entity.setToken(UUIDs.random());
        entity.setOwner(owner);
        entity.setJsonData(jsonUtil.toJson(tender));
        return entity;
    }

    private ResponseDto getResponseDto(final String cpId, final String token, final TenderDto tender) {
        final TenderResponseDto responseDto = new TenderResponseDto(
                token,
                cpId,
                tender.getDate(),
                tender.getPlanning(),
                tender.getTender(),
                tender.getParties(),
                tender.getBuyer()
        );
        return new ResponseDto<>(true, null, responseDto);
    }

}
