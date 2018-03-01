package com.procurement.access.service;

import com.procurement.access.dao.TenderDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.lots.LotDto;
import com.procurement.access.model.dto.lots.LotsRequestDto;
import com.procurement.access.model.dto.lots.LotsResponseDto;
import com.procurement.access.model.dto.lots.LotsUpdateResponseDto;
import com.procurement.access.model.dto.ocds.Lot;
import com.procurement.access.model.dto.ocds.TenderStatus;
import com.procurement.access.model.dto.ocds.TenderStatusDetails;
import com.procurement.access.model.dto.tender.TenderDto;
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
    private final TenderDao tenderDao;
    private final JsonUtil jsonUtil;

    public LotsServiceImpl(final TenderDao tenderDao,
                           final JsonUtil jsonUtil) {
        this.tenderDao = tenderDao;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public ResponseDto getLots(final String cpId, final TenderStatus status) {
        final TenderEntity entity = Optional.ofNullable(tenderDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException(DATA_NOT_FOUND_ERROR));
        final TenderDto tender = jsonUtil.toObject(TenderDto.class, entity.getJsonData());
        final LotsResponseDto lotsResponseDto = new LotsResponseDto(entity.getOwner(),
                getLotsDtoByStatus(tender.getTender().getLots(), status));
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
        final TenderEntity entity = Optional.ofNullable(tenderDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException(DATA_NOT_FOUND_ERROR));
        final TenderDto tender = jsonUtil.toObject(TenderDto.class, entity.getJsonData());
        final List<Lot> updatedLots = setLotsStatus(tender.getTender().getLots(), lotsDto, status);
        tender.getTender().setLots(updatedLots);
        entity.setJsonData(jsonUtil.toJson(tender));
        tenderDao.save(entity);
        return new ResponseDto<>(true, null, new LotsUpdateResponseDto(updatedLots));
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
        final TenderEntity entity = Optional.ofNullable(tenderDao.getByCpId(cpId))
                .orElseThrow(() -> new ErrorException(DATA_NOT_FOUND_ERROR));
        final TenderDto tender = jsonUtil.toObject(TenderDto.class, entity.getJsonData());
        final List<Lot> updatedLots = setLotsStatusDetails(tender.getTender().getLots(), lotsDto, statusDetails);
        tender.getTender().setLots(updatedLots);
        entity.setJsonData(jsonUtil.toJson(tender));
        tenderDao.save(entity);
        return new ResponseDto<>(true, null, new LotsUpdateResponseDto(updatedLots));
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
