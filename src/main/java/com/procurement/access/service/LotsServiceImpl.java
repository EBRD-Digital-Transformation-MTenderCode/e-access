package com.procurement.access.service;

import com.procurement.access.dao.TenderProcessDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.exception.ErrorType;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.lots.LotDto;
import com.procurement.access.model.dto.lots.LotsRequestDto;
import com.procurement.access.model.dto.lots.LotsResponseDto;
import com.procurement.access.model.dto.lots.LotsUpdateResponseDto;
import com.procurement.access.model.dto.ocds.Lot;
import com.procurement.access.model.dto.ocds.TenderStatus;
import com.procurement.access.model.dto.ocds.TenderStatusDetails;
import com.procurement.access.model.dto.tender.TenderProcessResponseDto;
import com.procurement.access.model.entity.TenderProcessEntity;
import com.procurement.access.utils.JsonUtil;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LotsServiceImpl implements LotsService {

    private final TenderProcessDao tenderProcessDao;
    private final JsonUtil jsonUtil;

    public LotsServiceImpl(final TenderProcessDao tenderProcessDao,
                           final JsonUtil jsonUtil) {
        this.tenderProcessDao = tenderProcessDao;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public ResponseDto getLots(final String cpId, final TenderStatus status) {
        final TenderProcessEntity entity = Optional.ofNullable(tenderProcessDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException(ErrorType.DATA_NOT_FOUND));
        final TenderProcessResponseDto process = jsonUtil.toObject(TenderProcessResponseDto.class, entity.getJsonData());
        final LotsResponseDto lotsResponseDto = new LotsResponseDto(entity.getOwner(),
                getLotsDtoByStatus(process.getTender().getLots(), status));
        return new ResponseDto<>(true, null, lotsResponseDto);
    }

    @Override
    public ResponseDto updateStatus(final String cpId, final TenderStatus status, final LotsRequestDto lotsDto) {
        final TenderProcessEntity entity = Optional.ofNullable(tenderProcessDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException(ErrorType.DATA_NOT_FOUND));
        final TenderProcessResponseDto process = jsonUtil.toObject(TenderProcessResponseDto.class, entity.getJsonData());
        final List<Lot> updatedLots = setLotsStatus(process.getTender().getLots(), lotsDto, status);
        process.getTender().setLots(updatedLots);
        entity.setJsonData(jsonUtil.toJson(process));
        tenderProcessDao.save(entity);
        return new ResponseDto<>(true, null, new LotsUpdateResponseDto(updatedLots));
    }

    @Override
    public ResponseDto updateStatusDetails(final String cpId,
                                           final TenderStatusDetails statusDetails,
                                           final LotsRequestDto lotsDto) {
        final TenderProcessEntity entity = Optional.ofNullable(tenderProcessDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException(ErrorType.DATA_NOT_FOUND));
        final TenderProcessResponseDto process = jsonUtil.toObject(TenderProcessResponseDto.class, entity.getJsonData());
        final List<Lot> updatedLots = setLotsStatusDetails(process.getTender().getLots(), lotsDto, statusDetails);
        process.getTender().setLots(updatedLots);
        entity.setJsonData(jsonUtil.toJson(process));
        tenderProcessDao.save(entity);
        return new ResponseDto<>(true, null, new LotsUpdateResponseDto(updatedLots));
    }

    private List<LotDto> getLotsDtoByStatus(final List<Lot> lots, final TenderStatus status) {
        if (lots.isEmpty()) throw new ErrorException(ErrorType.NO_ACTIVE_LOTS);
        final List<LotDto> lotsByStatus = lots.stream()
                .filter(l -> l.getStatus().equals(status))
                .map(l -> new LotDto(l.getId())).collect(Collectors.toList());
        if (lotsByStatus.isEmpty()) throw new ErrorException(ErrorType.NO_ACTIVE_LOTS);
        return lotsByStatus;
    }

    private List<Lot> setLotsStatus(final List<Lot> lots, final LotsRequestDto lotsDto, final TenderStatus status) {
        if (lots.isEmpty()) throw new ErrorException(ErrorType.NO_ACTIVE_LOTS);
        final Map<String, Lot> lotsMap = new HashMap<>();
        lots.forEach(lot -> lotsMap.put(lot.getId(), lot));
        lotsDto.getLots().forEach(lotDto -> lotsMap.get(lotDto.getId()).setStatus(status));
        return new ArrayList<>(lotsMap.values());
    }

    private List<Lot> setLotsStatusDetails(final List<Lot> lots,
                                           final LotsRequestDto lotsDto,
                                           final TenderStatusDetails statusDetails) {
        if (lots.isEmpty()) throw new ErrorException(ErrorType.NO_ACTIVE_LOTS);
        final Map<String, Lot> lotsMap = new HashMap<>();
        lots.forEach(lot -> lotsMap.put(lot.getId(), lot));
        lotsDto.getLots().forEach(lotDto -> lotsMap.get(lotDto.getId())
                .setStatusDetails(statusDetails));
        return new ArrayList<>(lotsMap.values());
    }
}
