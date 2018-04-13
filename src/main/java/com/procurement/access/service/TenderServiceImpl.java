package com.procurement.access.service;

import com.procurement.access.dao.TenderProcessDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.exception.ErrorType;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.cn.CnProcess;
import com.procurement.access.model.dto.ocds.TenderStatus;
import com.procurement.access.model.dto.ocds.TenderStatusDetails;
import com.procurement.access.model.dto.cn.TenderStatusResponseDto;
import com.procurement.access.model.entity.TenderProcessEntity;
import com.procurement.access.utils.JsonUtil;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TenderServiceImpl implements TenderService {

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
                .orElseThrow(() -> new ErrorException(ErrorType.DATA_NOT_FOUND));
        final CnProcess process = jsonUtil.toObject(CnProcess.class, entity.getJsonData());
        process.getTender().setStatus(status);
        entity.setJsonData(jsonUtil.toJson(process));
        tenderProcessDao.save(entity);
        return new ResponseDto<>(true, null,
                new TenderStatusResponseDto(status.value(), process.getTender().getStatusDetails().value()));
    }

    @Override
    public ResponseDto updateStatusDetails(final String cpId, final TenderStatusDetails statusDetails) {
        final TenderProcessEntity entity = Optional.ofNullable(tenderProcessDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException(ErrorType.DATA_NOT_FOUND));
        final CnProcess process = jsonUtil.toObject(CnProcess.class, entity.getJsonData());
        process.getTender().setStatusDetails(statusDetails);
        entity.setJsonData(jsonUtil.toJson(process));
        tenderProcessDao.save(entity);
        return new ResponseDto<>(true, null,
                new TenderStatusResponseDto(process.getTender().getStatus().value(),
                        process.getTender().getStatusDetails().value()));
    }

    @Override
    public ResponseDto setSuspended(final String cpId, final Boolean suspended) {
        final TenderProcessEntity entity = Optional.ofNullable(tenderProcessDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException(ErrorType.DATA_NOT_FOUND));
        final CnProcess process = jsonUtil.toObject(CnProcess.class, entity.getJsonData());
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
