package com.procurement.access.service;

import com.procurement.access.dao.TenderDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ocds.TenderStatus;
import com.procurement.access.model.dto.ocds.TenderStatusDetails;
import com.procurement.access.model.dto.tender.TenderDto;
import com.procurement.access.model.dto.tender.TenderStatusResponseDto;
import com.procurement.access.model.entity.TenderEntity;
import com.procurement.access.utils.JsonUtil;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TenderServiceImpl implements TenderService {

    private static final String DATA_NOT_FOUND_ERROR = "Data not found.";
    private final TenderDao tenderDao;
    private final JsonUtil jsonUtil;

    public TenderServiceImpl(final TenderDao tenderDao,
                             final JsonUtil jsonUtil) {
        this.tenderDao = tenderDao;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public ResponseDto updateStatus(final String cpId, final TenderStatus status) {
        final TenderEntity entity = Optional.ofNullable(tenderDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException(DATA_NOT_FOUND_ERROR));
        final TenderDto tender = jsonUtil.toObject(TenderDto.class, entity.getJsonData());
        tender.getTender().setStatus(status);
        entity.setJsonData(jsonUtil.toJson(tender));
        tenderDao.save(entity);
        return new ResponseDto<>(true, null,
                new TenderStatusResponseDto(status.value(), tender.getTender().getStatusDetails().value()));
    }

    @Override
    public ResponseDto updateStatusDetails(final String cpId, final TenderStatusDetails statusDetails) {
        final TenderEntity entity = Optional.ofNullable(tenderDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException(DATA_NOT_FOUND_ERROR));
        final TenderDto tender = jsonUtil.toObject(TenderDto.class, entity.getJsonData());
        tender.getTender().setStatusDetails(statusDetails);
        entity.setJsonData(jsonUtil.toJson(tender));
        tenderDao.save(entity);
        return new ResponseDto<>(true, null,
                new TenderStatusResponseDto(tender.getTender().getStatus().value(),
                        tender.getTender().getStatusDetails().value()));
    }

    @Override
    public ResponseDto setSuspended(String cpId, Boolean suspended) {
        final TenderEntity entity = Optional.ofNullable(tenderDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException(DATA_NOT_FOUND_ERROR));
        final TenderDto tender = jsonUtil.toObject(TenderDto.class, entity.getJsonData());
        if (suspended) {
            tender.getTender().setStatusDetails(TenderStatusDetails.SUSPENDED);
        } else {
            tender.getTender().setStatusDetails(TenderStatusDetails.EMPTY);
        }
        entity.setJsonData(jsonUtil.toJson(tender));
        tenderDao.save(entity);
        return new ResponseDto<>(true, null,
                new TenderStatusResponseDto(tender.getTender().getStatus().value(),
                        tender.getTender().getStatusDetails().value()));
    }
}
