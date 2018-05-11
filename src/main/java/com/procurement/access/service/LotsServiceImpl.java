package com.procurement.access.service;

import com.procurement.access.dao.TenderProcessDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.exception.ErrorType;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.lots.*;
import com.procurement.access.model.dto.ocds.*;
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
    public ResponseDto getLots(final String cpId, final String stage, final TenderStatus status) {
        final TenderProcessEntity entity = Optional.ofNullable(tenderProcessDao.getByCpIdAndStage(cpId, stage))
                .orElseThrow(() -> new ErrorException(ErrorType.DATA_NOT_FOUND));
        final TenderProcess process = jsonUtil.toObject(TenderProcess.class, entity.getJsonData());
        final LotsResponseDto lotsResponseDto = new LotsResponseDto(
                process.getTender().getAwardCriteria().value(),
                getLotsDtoByStatus(process.getTender().getLots(), status));
        return new ResponseDto<>(true, null, lotsResponseDto);
    }

    @Override
    public ResponseDto updateStatus(final String cpId, final String stage, final TenderStatus status, final LotsRequestDto lotsDto) {
        final TenderProcessEntity entity = Optional.ofNullable(tenderProcessDao.getByCpIdAndStage(cpId, stage))
                .orElseThrow(() -> new ErrorException(ErrorType.DATA_NOT_FOUND));
        final TenderProcess process = jsonUtil.toObject(TenderProcess.class, entity.getJsonData());
        final List<Lot> updatedLots = setLotsStatus(process.getTender().getLots(), lotsDto, status);
        final Tender tender = process.getTender();
        tender.setLots(updatedLots);
        entity.setJsonData(jsonUtil.toJson(process));
        tenderProcessDao.save(entity);
        return new ResponseDto<>(true, null,
                new LotsUpdateResponseDto(tender.getStatus(), updatedLots, null));
    }

    @Override
    public ResponseDto updateStatusDetails(final String cpId,
                                           final String stage,
                                           final TenderStatusDetails statusDetails,
                                           final LotsRequestDto lotsDto) {
        final TenderProcessEntity entity = Optional.ofNullable(tenderProcessDao.getByCpIdAndStage(cpId, stage))
                .orElseThrow(() -> new ErrorException(ErrorType.DATA_NOT_FOUND));
        final TenderProcess process = jsonUtil.toObject(TenderProcess.class, entity.getJsonData());
        final List<Lot> updatedLots = setLotsStatusDetails(process.getTender().getLots(), lotsDto, statusDetails);
        final Tender tender = process.getTender();
        tender.setLots(updatedLots);
        if (isAllLotsUnsuccessful(tender.getLots())) {
            tender.setStatus(TenderStatus.UNSUCCESSFUL);
            tender.setStatusDetails(TenderStatusDetails.EMPTY);
        }
        entity.setJsonData(jsonUtil.toJson(process));
        tenderProcessDao.save(entity);
        return new ResponseDto<>(true, null,
                new LotsUpdateResponseDto(tender.getStatus(), updatedLots, null));
    }

    @Override
    public ResponseDto updateStatusDetailsById(final String cpId,
                                               final String stage,
                                               final String lotId,
                                               final TenderStatusDetails statusDetails) {
        final TenderProcessEntity entity = Optional.ofNullable(tenderProcessDao.getByCpIdAndStage(cpId, stage))
                .orElseThrow(() -> new ErrorException(ErrorType.DATA_NOT_FOUND));
        final TenderProcess process = jsonUtil.toObject(TenderProcess.class, entity.getJsonData());
        final Lot updatedLot = process.getTender().getLots().stream()
                .filter(lot -> lot.getId().equals(lotId))
                .findFirst().orElseThrow(() -> new ErrorException(ErrorType.DATA_NOT_FOUND));
        updatedLot.setStatusDetails(statusDetails);
        entity.setJsonData(jsonUtil.toJson(process));
        tenderProcessDao.save(entity);
        return new ResponseDto<>(true, null, new LotUpdateResponseDto(updatedLot));
    }

    @Override
    public ResponseDto checkStatusDetails(String cpId, String stage) {
        final TenderProcessEntity entity = Optional.ofNullable(tenderProcessDao.getByCpIdAndStage(cpId, stage))
                .orElseThrow(() -> new ErrorException(ErrorType.DATA_NOT_FOUND));
        final TenderProcess process = jsonUtil.toObject(TenderProcess.class, entity.getJsonData());
        if (process.getTender().getLots().stream()
                .filter(lot -> lot.getStatus().equals(TenderStatus.ACTIVE))
                .anyMatch(lot -> !(lot.getStatusDetails().equals(TenderStatusDetails.AWARDED)))) {
            throw new ErrorException(ErrorType.NOT_ALL_LOTS_AWARDED);
        }
        return new ResponseDto<>(true, null, "All active lots are awarded.");
    }

    @Override
    public ResponseDto updateLots(final String cpId, final String stage, final LotsRequestDto lotsDto) {
        final TenderProcessEntity entity = Optional.ofNullable(tenderProcessDao.getByCpIdAndStage(cpId, stage))
                .orElseThrow(() -> new ErrorException(ErrorType.DATA_NOT_FOUND));
        final TenderProcess process = jsonUtil.toObject(TenderProcess.class, entity.getJsonData());
        final List<Lot> updatedLots = updateLots(process.getTender().getLots(), lotsDto);
        final Tender tender = process.getTender();
        tender.setLots(updatedLots);
        List<Item> items = null;
        if (isAllLotsUnsuccessful(tender.getLots())) {
            tender.setStatus(TenderStatus.UNSUCCESSFUL);
            tender.setStatusDetails(TenderStatusDetails.EMPTY);
        } else {
            items = getItemsForCompiledLots(process.getTender().getItems(), updatedLots);
        }
        entity.setJsonData(jsonUtil.toJson(process));
        tenderProcessDao.save(entity);
        return new ResponseDto<>(true, null,
                new LotsUpdateResponseDto(tender.getStatus(), updatedLots, items));
    }

    private List<Item> getItemsForCompiledLots(final Set<Item> items, final List<Lot> lots) {
        final List<String> compiledLots = lots.stream()
                .filter(lot -> (lot.getStatus().equals(TenderStatus.COMPLETE)
                        && lot.getStatusDetails().equals(TenderStatusDetails.EMPTY)))
                .map(lot -> lot.getId())
                .collect(Collectors.toList());
        return items.stream()
                .filter(item -> compiledLots.contains(item.getRelatedLot()))
                .collect(Collectors.toList());
    }

    private List<Lot> updateLots(final List<Lot> lots, final LotsRequestDto unsuccessfulLots) {
        if (lots.isEmpty()) throw new ErrorException(ErrorType.NO_ACTIVE_LOTS);
        final Set<String> unsuccessfulLotIds = unsuccessfulLots.getLots().stream()
                .map(lot -> lot.getId())
                .collect(Collectors.toSet());
        lots.forEach(lot -> {
            if (lot.getStatus().equals(TenderStatus.ACTIVE) && lot.getStatusDetails().equals(TenderStatusDetails.AWARDED)) {
                lot.setStatus(TenderStatus.COMPLETE);
                lot.setStatusDetails(TenderStatusDetails.EMPTY);
            }
            if (unsuccessfulLotIds.contains(lot.getId())) {
                lot.setStatus(TenderStatus.UNSUCCESSFUL);
                lot.setStatusDetails(TenderStatusDetails.EMPTY);
            }
        });
        return lots;
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
        lots.forEach(lot -> {
            lotsMap.put(lot.getId(), lot);
            if (lot.getStatusDetails().equals(TenderStatusDetails.UNSUCCESSFUL))
                lot.setStatusDetails(TenderStatusDetails.EMPTY);
        });
        lotsDto.getLots().forEach(lotDto -> lotsMap.get(lotDto.getId()).setStatus(status));
        return new ArrayList<>(lotsMap.values());
    }

    private List<Lot> setLotsStatusDetails(final List<Lot> lots,
                                           final LotsRequestDto lotsDto,
                                           final TenderStatusDetails statusDetails) {
        if (lots.isEmpty()) throw new ErrorException(ErrorType.NO_ACTIVE_LOTS);
        final Map<String, Lot> lotsMap = new HashMap<>();
        lots.forEach(lot -> lotsMap.put(lot.getId(), lot));
        lotsDto.getLots().forEach(lotDto -> lotsMap.get(lotDto.getId()).setStatusDetails(statusDetails));
        return new ArrayList<>(lotsMap.values());
    }

    private Boolean isAllLotsUnsuccessful(final List<Lot> lots) {
        if (lots != null && !lots.isEmpty()) {
            return lots.stream().allMatch(lot -> lot.getStatus().equals(TenderStatus.UNSUCCESSFUL));
        }
        return true;
    }
}
