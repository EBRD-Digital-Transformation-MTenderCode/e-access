package com.procurement.access.service;

import com.procurement.access.dao.TenderProcessDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ocds.TenderStatus;
import com.procurement.access.model.dto.ocds.TenderStatusDetails;
import com.procurement.access.model.dto.tender.TenderProcessDto;
import com.procurement.access.model.dto.tender.TenderStatusResponseDto;
import com.procurement.access.model.entity.TenderProcessEntity;
import com.procurement.access.utils.JsonUtil;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TenderServiceImpl implements TenderService {

    private static final String DATA_NOT_FOUND_ERROR = "Data not found.";
    private final TenderProcessDao tenderProcessDao;
    private final JsonUtil jsonUtil;

    public TenderServiceImpl(final TenderProcessDao tenderProcessDao,
                             final JsonUtil jsonUtil) {
        this.tenderProcessDao = tenderProcessDao;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public ResponseDto updateStatus(final String cpId, final TenderStatus status) {
        final TenderProcessEntity entity = Optional.ofNullable(tenderProcessDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException(DATA_NOT_FOUND_ERROR));
        final TenderProcessDto process = jsonUtil.toObject(TenderProcessDto.class, entity.getJsonData());
        process.getTender().setStatus(status);
        entity.setJsonData(jsonUtil.toJson(process));
        tenderProcessDao.save(entity);
        return new ResponseDto<>(true, null,
                new TenderStatusResponseDto(status.value(), process.getTender().getStatusDetails().value()));
    }

    @Override
    public ResponseDto updateStatusDetails(final String cpId, final TenderStatusDetails statusDetails) {
        final TenderProcessEntity entity = Optional.ofNullable(tenderProcessDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException(DATA_NOT_FOUND_ERROR));
        final TenderProcessDto process = jsonUtil.toObject(TenderProcessDto.class, entity.getJsonData());
        process.getTender().setStatusDetails(statusDetails);
        entity.setJsonData(jsonUtil.toJson(process));
        tenderProcessDao.save(entity);
        return new ResponseDto<>(true, null,
                new TenderStatusResponseDto(process.getTender().getStatus().value(),
                        process.getTender().getStatusDetails().value()));
    }

    @Override
    public ResponseDto setSuspended(String cpId, Boolean suspended) {
        final TenderProcessEntity entity = Optional.ofNullable(tenderProcessDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException(DATA_NOT_FOUND_ERROR));
        final TenderProcessDto process = jsonUtil.toObject(TenderProcessDto.class, entity.getJsonData());
        if (suspended) {
            process.getTender().setStatusDetails(TenderStatusDetails.SUSPENDED);
        } else {
            process.getTender().setStatusDetails(TenderStatusDetails.EMPTY);
        }
        entity.setJsonData(jsonUtil.toJson(process));
        tenderProcessDao.save(entity);
        return new ResponseDto<>(true, null,
                new TenderStatusResponseDto(process.getTender().getStatus().value(),
                        process.getTender().getStatusDetails().value()));
    }
}
