package com.procurement.access.service;

import com.datastax.driver.core.utils.UUIDs;
import com.procurement.access.config.properties.OCDSProperties;
import com.procurement.access.dao.EiDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ein.EiDto;
import com.procurement.access.model.dto.ein.EiResponseDto;
import com.procurement.access.model.dto.ocds.TenderStatus;
import com.procurement.access.model.entity.EiEntity;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class EiServiceImpl implements EiService {

    private static final String DATA_NOT_FOUND_ERROR = "Data not found.";
    private static final String INVALID_OWNER_ERROR = "Invalid owner.";
    private final OCDSProperties ocdsProperties;
    private final JsonUtil jsonUtil;
    private final DateUtil dateUtil;
    private final EiDao eiDao;

    public EiServiceImpl(final OCDSProperties ocdsProperties,
                         final JsonUtil jsonUtil,
                         final DateUtil dateUtil,
                         final EiDao eiDao) {
        this.ocdsProperties = ocdsProperties;
        this.jsonUtil = jsonUtil;
        this.dateUtil = dateUtil;
        this.eiDao = eiDao;
    }

    @Override
    public ResponseDto createEi(final String owner, final EiDto ei) {
        final String cpId = ocdsProperties.getPrefix() + dateUtil.getMilliNowUTC();
        ei.setOcId(cpId);
        ei.setDate(dateUtil.getNowUTC());
        ei.setId(UUIDs.timeBased().toString());
        setTenderId(ei, cpId);
        setTenderStatus(ei);
        setBudgetId(ei);
        final EiEntity entity = getEntity(ei, owner);
        eiDao.save(entity);
        return getResponseDto(cpId, entity.getToken(), ei);
    }

    @Override
    public ResponseDto updateEi(final String owner,
                                final String cpId,
                                final String token,
                                final EiDto eiDto) {

        final EiEntity entity = Optional.ofNullable(eiDao.getByCpIdAndToken(cpId, token))
                .orElseThrow(() -> new ErrorException(DATA_NOT_FOUND_ERROR));
        if (!entity.getOwner().equals(owner)) throw new ErrorException(INVALID_OWNER_ERROR);
        final EiDto ei = jsonUtil.toObject(EiDto.class, entity.getJsonData());
        ei.setPlanning(eiDto.getPlanning());
        ei.setTender(eiDto.getTender());
        entity.setJsonData(jsonUtil.toJson(ei));
        eiDao.save(entity);
        return getResponseDto(cpId, entity.getToken(), ei);
    }

    private void setTenderId(final EiDto ei, final String cpId) {
        ei.getTender().setId(cpId);
    }

    private void setTenderStatus(final EiDto ei) {
        ei.getTender().setStatus(TenderStatus.PLANNING);
    }

    private void setBudgetId(final EiDto ei) {
        ei.getPlanning().getBudget().setId(UUIDs.timeBased().toString());
    }

    private EiEntity getEntity(final EiDto ei, final String owner) {
        final EiEntity eiEntity = new EiEntity();
        eiEntity.setCpId(ei.getOcId());
        eiEntity.setToken(UUIDs.timeBased().toString());
        eiEntity.setOwner(owner);
        eiEntity.setJsonData(jsonUtil.toJson(ei));
        return eiEntity;
    }

    private ResponseDto getResponseDto(final String cpId, final String token, final EiDto ei) {
        final EiResponseDto responseDto = new EiResponseDto(
                token,
                cpId,
                ei.getId(),
                ei.getDate(),
                ei.getPlanning(),
                ei.getTender(),
                ei.getParties(),
                ei.getBuyer()
        );
        return new ResponseDto<>(true, null, responseDto);
    }
}
