package com.procurement.access.service;

import com.procurement.access.dao.CnDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.cn.CnDto;
import com.procurement.access.model.dto.lots.LotDto;
import com.procurement.access.model.dto.lots.LotsRequestDto;
import com.procurement.access.model.dto.lots.LotsResponseDto;
import com.procurement.access.model.dto.ocds.Lot;
import com.procurement.access.model.dto.ocds.TenderStatus;
import com.procurement.access.model.dto.ocds.TenderStatusDetails;
import com.procurement.access.model.entity.TenderEntity;
import com.procurement.access.utils.JsonUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LotsServiceImpl implements LotsService {

    private static final String DATA_NOT_FOUND_ERROR = "Data not found.";
    private final CnDao cnDao;
    private final JsonUtil jsonUtil;

    public LotsServiceImpl(final CnDao cnDao,
                           final JsonUtil jsonUtil) {
        this.cnDao = cnDao;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public ResponseDto getLots(final String cpId, final TenderStatus status) {
        final TenderEntity entity = Optional.ofNullable(cnDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException(DATA_NOT_FOUND_ERROR));
        final CnDto cn = jsonUtil.toObject(CnDto.class, entity.getJsonData());
        final LotsResponseDto lotsResponseDto = new LotsResponseDto(entity.getOwner(),
                getLotsDtoByStatus(cn.getTender().getLots(), status));
        return new ResponseDto<>(true, null, lotsResponseDto);
    }

    private List<LotDto> getLotsDtoByStatus(final List<Lot> lots, final TenderStatus status) {
        if (lots.isEmpty()) throw new ErrorException(DATA_NOT_FOUND_ERROR);
        final List<LotDto> lotsByStatus = lots.stream()
                .filter(l -> l.getStatus().equals(status))
                .map(l -> new LotDto(l.getId())).collect(Collectors.toList());
        if (lotsByStatus.isEmpty()) throw new ErrorException(DATA_NOT_FOUND_ERROR);
        return lotsByStatus;
    }

    @Override
    public ResponseDto updateStatus(final String cpId, final TenderStatus status, final LotsRequestDto lotsDto) {
        final TenderEntity entity = Optional.ofNullable(cnDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException(DATA_NOT_FOUND_ERROR));
        final CnDto cn = jsonUtil.toObject(CnDto.class, entity.getJsonData());
        final List<Lot> updatedLots = setLotsStatus(cn.getTender().getLots(), lotsDto, status);
        cn.getTender().setLots(updatedLots);
        entity.setJsonData(jsonUtil.toJson(cn));
        cnDao.save(entity);
        return new ResponseDto<>(true, null, updatedLots);
    }

    private List<Lot> setLotsStatus(final List<Lot> lots, final LotsRequestDto lotsDto, final TenderStatus status) {
        if (lots.isEmpty()) throw new ErrorException(DATA_NOT_FOUND_ERROR);
        final Map<String, Lot> lotsMap = new HashMap<>();
        lots.forEach(lot -> lotsMap.put(lot.getId(), lot));
        lotsDto.getLots().forEach(lotDto -> lotsMap.get(lotDto.getId()).setStatus(status));
        return lotsMap.values().stream().collect(Collectors.toList());
    }

    @Override
    public ResponseDto updateStatusDetails(final String cpId,
                                           final TenderStatusDetails statusDetails,
                                           final LotsRequestDto lotsDto) {
        final TenderEntity entity = Optional.ofNullable(cnDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException(DATA_NOT_FOUND_ERROR));
        final CnDto cn = jsonUtil.toObject(CnDto.class, entity.getJsonData());
        final List<Lot> updatedLots = setLotsStatusDetails(cn.getTender().getLots(), lotsDto, statusDetails);
        cn.getTender().setLots(updatedLots);
        entity.setJsonData(jsonUtil.toJson(cn));
        cnDao.save(entity);
        return new ResponseDto<>(true, null, updatedLots);
    }

    private List<Lot> setLotsStatusDetails(final List<Lot> lots,
                                           final LotsRequestDto lotsDto,
                                           final TenderStatusDetails statusDetails) {
        if (lots.isEmpty()) throw new ErrorException(DATA_NOT_FOUND_ERROR);
        final Map<String, Lot> lotsMap = new HashMap<>();
        lots.forEach(lot -> lotsMap.put(lot.getId(), lot));
        lotsDto.getLots().forEach(lotDto -> lotsMap.get(lotDto.getId())
                .setStatusDetails(statusDetails));
        return lotsMap.values().stream().collect(Collectors.toList());
    }
}
