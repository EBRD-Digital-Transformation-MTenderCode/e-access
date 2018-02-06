package com.procurement.access.service;

import com.datastax.driver.core.utils.UUIDs;
import com.procurement.access.config.properties.OCDSProperties;
import com.procurement.access.dao.TenderDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.cn.TenderDto;
import com.procurement.access.model.dto.cn.TenderResponseDto;
import com.procurement.access.model.entity.TenderEntity;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;

import static com.procurement.access.model.dto.ocds.TenderStatus.ACTIVE;

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
                                final TenderDto tender) {
        setTenderId(tender);
        setItemsId(tender);
        setLotsIdAndItemsRelatedLots(tender);
        setTenderStatus(tender);
        final TenderEntity entity = getEntity(tender, owner);
        tenderDao.save(entity);
        return getResponseDto(entity.getCpId(), entity.getToken(), tender);
    }

    @Override
    public ResponseDto updateCn(final String owner,
                                final String cpId,
                                final String token,
                                final TenderDto tenderDto) {
        final TenderEntity entity = Optional.ofNullable(tenderDao.getByCpIdAndToken(cpId, token))
                .orElseThrow(() -> new ErrorException(DATA_NOT_FOUND_ERROR));
        if (!entity.getOwner().equals(owner)) throw new ErrorException(INVALID_OWNER_ERROR);
        final TenderDto tender = jsonUtil.toObject(TenderDto.class, entity.getJsonData());
        tender.setTender(tenderDto.getTender());
        entity.setJsonData(jsonUtil.toJson(tender));
        tenderDao.save(entity);
        return getResponseDto(cpId, entity.getToken(), tender);
    }


    private void setTenderId(final TenderDto tender) {
        if (Objects.isNull(tender.getTender().getId())) {
            tender.getTender().setId(ocdsProperties.getPrefix() + dateUtil.getMilliNowUTC());
        }
    }

    private void setTenderStatus(final TenderDto tender) {
        tender.getTender().setStatus(ACTIVE);
    }

    private void setItemsId(final TenderDto tenderDto) {
        tenderDto.getTender().getItems().forEach(i -> {
            i.setId(UUIDs.timeBased().toString());
        });
    }

    private void setLotsIdAndItemsRelatedLots(final TenderDto tender) {
        tender.getTender().getLots().forEach(l -> {
            final String id = UUIDs.timeBased().toString();
            tender.getTender().getItems()
                    .stream()
                    .filter(i -> i.getRelatedLot().equals(l.getId()))
                    .findFirst()
                    .get()
                    .setRelatedLot(id);

            l.setId(id);
        });
    }

//    private RelatedProcess getFsRelatedProcess(final TenderDto cn) {
//        return cn.getRelatedProcesses()
//                .stream()
//                .filter(rp -> rp.getRelationship().contains(RelatedProcess.RelatedProcessType.X_BUDGET))
//                .filter(rp -> rp.getScheme().equals(RelatedProcess.RelatedProcessScheme.OCID))
//                .filter(rp -> !rp.getIdentifier().isEmpty())
//                .findFirst().orElse(null);
//    }

    private TenderEntity getEntity(final TenderDto tender, final String owner) {
        final TenderEntity entity = new TenderEntity();
        entity.setCpId(tender.getTender().getId());
        entity.setToken(UUIDs.timeBased().toString());
        entity.setOwner(owner);
        entity.setJsonData(jsonUtil.toJson(tender));
        return entity;
    }

    private ResponseDto getResponseDto(final String cpId, final String token, final TenderDto tender) {
        final TenderResponseDto responseDto = new TenderResponseDto(
                token,
                cpId,
                tender.getPlanning(),
                tender.getTender(),
                tender.getParties(),
                tender.getBuyer(),
                tender.getRelatedProcesses()
        );
        return new ResponseDto<>(true, null, responseDto);
    }

}
