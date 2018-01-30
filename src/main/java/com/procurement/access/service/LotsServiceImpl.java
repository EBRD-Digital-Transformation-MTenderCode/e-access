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
import com.procurement.access.model.entity.CnEntity;
import com.procurement.access.utils.JsonUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LotsServiceImpl implements LotsService {

    private final CnDao cnDao;
    private final JsonUtil jsonUtil;

    public LotsServiceImpl(final CnDao cnDao,
                           final JsonUtil jsonUtil) {
        this.cnDao = cnDao;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public ResponseDto getLots(String cpId, TenderStatus status) {
        final CnEntity entity = Optional.ofNullable(cnDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException("Data not found."));
        final CnDto cn = jsonUtil.toObject(CnDto.class, entity.getJsonData());
        LotsResponseDto lotsResponseDto = new LotsResponseDto(entity.getOwner(), getLotsDtoByStatus(cn, status));
        return new ResponseDto<>(true, null, lotsResponseDto);
    }

    private List<LotDto> getLotsDtoByStatus(CnDto cn, TenderStatus status) {
        return cn.getTender().getLots().stream()
                .filter(l -> l.getStatus().equals(status))
                .map(l -> new LotDto(l.getId())).collect(Collectors.toList());
    }

    @Override
    public ResponseDto updateStatus(String cpId, TenderStatus status, LotsRequestDto lotsDto) {
        final CnEntity entity = Optional.ofNullable(cnDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException("Data not found."));
        final CnDto cn = jsonUtil.toObject(CnDto.class, entity.getJsonData());
        List<Lot> updatedLots = setLotsStatus(cn.getTender().getLots(), lotsDto, status);
        cn.getTender().setLots(updatedLots);
        entity.setJsonData(jsonUtil.toJson(cn));
        cnDao.save(entity);
        return new ResponseDto<>(true, null, updatedLots);
    }

    private List<Lot> setLotsStatus(List<Lot> lots, LotsRequestDto lotsDto, TenderStatus status) {
        Map<String, Lot> lotsMap = new HashMap<>();
        lots.forEach(lot -> lotsMap.put(lot.getId(), lot));
        lotsDto.getLots().forEach(lotDto -> lotsMap.get(lotDto.getId()).setStatus(status));
        return lotsMap.values().stream().collect(Collectors.toList());
    }

    @Override
    public ResponseDto updateStatusDetails(String cpId, TenderStatusDetails statusDetails, LotsRequestDto lotsDto) {
        final CnEntity entity = Optional.ofNullable(cnDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException("Data not found."));
        final CnDto cn = jsonUtil.toObject(CnDto.class, entity.getJsonData());
        List<Lot> updatedLots = setLotsStatusDetails(cn.getTender().getLots(), lotsDto, statusDetails);
        cn.getTender().setLots(updatedLots);
        entity.setJsonData(jsonUtil.toJson(cn));
        cnDao.save(entity);
        return new ResponseDto<>(true, null, updatedLots);
    }

    private List<Lot> setLotsStatusDetails(List<Lot> lots, LotsRequestDto lotsDto, TenderStatusDetails statusDetails) {
        Map<String, Lot> lotsMap = new HashMap<>();
        lots.forEach(lot -> lotsMap.put(lot.getId(), lot));
        lotsDto.getLots().forEach(lotDto -> lotsMap.get(lotDto.getId())
                .setStatusDetails(statusDetails));
        return lotsMap.values().stream().collect(Collectors.toList());
    }
}
