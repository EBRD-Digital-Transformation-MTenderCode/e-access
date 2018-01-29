package com.procurement.access.service;

import com.procurement.access.dao.CnDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.cn.CnDto;
import com.procurement.access.model.dto.ocds.TenderStatus;
import com.procurement.access.model.dto.ocds.TenderStatusDetails;
import com.procurement.access.model.entity.CnEntity;
import com.procurement.access.utils.JsonUtil;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TenderServiceImpl implements TenderService {

    private final CnDao cnDao;
    private final JsonUtil jsonUtil;

    public TenderServiceImpl(final CnDao cnDao,
                             final JsonUtil jsonUtil) {
        this.cnDao = cnDao;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public ResponseDto updateStatus(String cpId, String status) {
        final CnEntity entity = Optional.ofNullable(cnDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException("Data not found."));
        final CnDto cn = jsonUtil.toObject(CnDto.class, entity.getJsonData());
        cn.getTender().setStatus(TenderStatus.fromValue(status));
        entity.setJsonData(jsonUtil.toJson(cn));
        cnDao.save(entity);
        return new ResponseDto<>(true, null, cn);
    }

    @Override
    public ResponseDto updateStatusDetails(String cpId, String statusDetails) {
        final CnEntity entity = Optional.ofNullable(cnDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException("Data not found."));
        final CnDto cn = jsonUtil.toObject(CnDto.class, entity.getJsonData());
        cn.getTender().setStatusDetails(TenderStatusDetails.fromValue(statusDetails));
        entity.setJsonData(jsonUtil.toJson(cn));
        cnDao.save(entity);
        return new ResponseDto<>(true, null, cn);
    }
}
