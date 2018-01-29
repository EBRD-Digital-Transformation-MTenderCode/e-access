package com.procurement.access.service;

import com.procurement.access.dao.CnDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.cn.CnDto;
import com.procurement.access.model.dto.lots.LotDto;
import com.procurement.access.model.dto.lots.LotsResponseDto;
import com.procurement.access.model.entity.CnEntity;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LotsServiceImpl implements LotsService {

    private final CnDao cnDao;
    private final JsonUtil jsonUtil;
    private final DateUtil dateUtil;

    public LotsServiceImpl(final CnDao cnDao,
                           final JsonUtil jsonUtil,
                           final DateUtil dateUtil) {
        this.cnDao = cnDao;
        this.jsonUtil = jsonUtil;
        this.dateUtil = dateUtil;
    }

    @Override
    public ResponseDto getLots(String cpId, String status) {
        final CnEntity entity = Optional.ofNullable(cnDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException("Data not found."));
        final CnDto cn = jsonUtil.toObject(CnDto.class, entity.getJsonData());
        LotsResponseDto lotsResponseDto = new LotsResponseDto(entity.getOwner(), getLotsDtoByStatus(cn, status));
        return new ResponseDto<>(true,null, lotsResponseDto);
    }

    private List<LotDto> getLotsDtoByStatus(CnDto cn, String status) {
        return cn.getTender().getLots().stream().filter(l -> l.getStatus().value().equals
                (status)).map(l -> new LotDto(l.getId())).collect(Collectors.toList());
    }

}
