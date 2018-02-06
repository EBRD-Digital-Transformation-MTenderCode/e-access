package com.procurement.access.service;

import com.procurement.access.dao.CnDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.cn.CnDto;
import com.procurement.access.model.dto.ocds.TenderStatus;
import com.procurement.access.model.dto.ocds.TenderStatusDetails;
import com.procurement.access.model.entity.TenderEntity;
import com.procurement.access.utils.JsonUtil;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TenderServiceImpl implements TenderService {

    private static final String DATA_NOT_FOUND_ERROR = "Data not found.";
    private final CnDao cnDao;
    private final JsonUtil jsonUtil;

    public TenderServiceImpl(final CnDao cnDao,
                             final JsonUtil jsonUtil) {
        this.cnDao = cnDao;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public ResponseDto updateStatus(final String cpId, final TenderStatus status) {
        final TenderEntity entity = Optional.ofNullable(cnDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException(DATA_NOT_FOUND_ERROR));
        final CnDto cn = jsonUtil.toObject(CnDto.class, entity.getJsonData());
        cn.getTender().setStatus(status);
        entity.setJsonData(jsonUtil.toJson(cn));
        cnDao.save(entity);
        return new ResponseDto<>(true, null, cn);
    }

    @Override
    public ResponseDto updateStatusDetails(final String cpId, final TenderStatusDetails statusDetails) {
        final TenderEntity entity = Optional.ofNullable(cnDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException(DATA_NOT_FOUND_ERROR));
        final CnDto cn = jsonUtil.toObject(CnDto.class, entity.getJsonData());
        cn.getTender().setStatusDetails(statusDetails);
        entity.setJsonData(jsonUtil.toJson(cn));
        cnDao.save(entity);
        return new ResponseDto<>(true, null, cn);
    }

    @Override
    public ResponseDto setSuspended(String cpId, Boolean suspended) {
        final TenderEntity entity = Optional.ofNullable(cnDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException(DATA_NOT_FOUND_ERROR));
        final CnDto cn = jsonUtil.toObject(CnDto.class, entity.getJsonData());
        if (suspended) {
            cn.getTender().setStatusDetails(TenderStatusDetails.SUSPENDED);
        } else {
            cn.getTender().setStatusDetails(null);
        }
        entity.setJsonData(jsonUtil.toJson(cn));
        cnDao.save(entity);
        return new ResponseDto<>(true, null, cn);
    }
}
