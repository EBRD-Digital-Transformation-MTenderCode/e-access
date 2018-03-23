package com.procurement.access.service;

import com.datastax.driver.core.utils.UUIDs;
import com.procurement.access.config.properties.OCDSProperties;
import com.procurement.access.dao.TenderDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ocds.OrganizationReference;
import com.procurement.access.model.dto.ocds.Tender;
import com.procurement.access.model.dto.tender.CnDto;
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

    private static final String SEPARATOR = "-";
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
                                final LocalDateTime dateTime,
                                final CnDto cn) {
        final String cpId = ocdsProperties.getPrefix() + dateUtil.getMilliNowUTC();
        cn.setOcId(cpId);
        final Tender tender = cn.getTender();
        setLotsStatus(tender);
        setTenderStatus(tender);
        tender.setId(cpId);
        setItemsId(tender);
        setLotsIdAndItemsRelatedLots(tender);
        setIdOfOrganizationReference(tender.getProcuringEntity());
        final TenderEntity entity = getEntity(cn, owner, dateTime);
        tenderDao.save(entity);
        cn.setToken(entity.getToken().toString());
        return new ResponseDto<>(true, null, cn);
    }

    @Override
    public ResponseDto updateCn(final String owner,
                                final String cpId,
                                final String token,
                                final CnDto cn) {
        final TenderEntity entity = Optional.ofNullable(tenderDao.getByCpIdAndToken(cpId, UUID.fromString(token)))
                .orElseThrow(() -> new ErrorException(DATA_NOT_FOUND_ERROR));
        if (!entity.getOwner().equals(owner)) throw new ErrorException(INVALID_OWNER_ERROR);
        final CnDto tender = jsonUtil.toObject(CnDto.class, entity.getJsonData());
        tender.setTender(cn.getTender());
        entity.setJsonData(jsonUtil.toJson(tender));
        tenderDao.save(entity);
        cn.setToken(entity.getToken().toString());
        return new ResponseDto<>(true, null, cn);
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

    private TenderEntity getEntity(final CnDto tender, final String owner, final LocalDateTime dateTime) {
        final TenderEntity entity = new TenderEntity();
        entity.setCpId(tender.getTender().getId());
        entity.setToken(UUIDs.random());
        entity.setOwner(owner);
        entity.setCreatedDate(dateUtil.localToDate(dateTime));
        entity.setJsonData(jsonUtil.toJson(tender));
        return entity;
    }


}
