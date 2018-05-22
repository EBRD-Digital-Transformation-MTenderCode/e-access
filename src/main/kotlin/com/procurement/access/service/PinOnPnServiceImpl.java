package com.procurement.access.service;

import com.procurement.access.model.dto.pin.PinLot;
import com.procurement.access.model.dto.pin.PinProcess;
import com.procurement.access.model.dto.pin.PinTender;
import com.procurement.access.model.dto.pn.PnLot;
import com.procurement.access.model.dto.pn.PnProcess;
import com.procurement.access.model.dto.pn.PnTender;
import com.procurement.access.model.entity.TenderProcessEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import static com.procurement.access.model.dto.ocds.TenderStatus.PLANNED;
import static com.procurement.access.model.dto.ocds.TenderStatusDetails.EMPTY;

@Service
public class PinOnPnServiceImpl implements PinOnPnService {

    private final JsonUtil jsonUtil;
    private final DateUtil dateUtil;
    private final TenderProcessDao tenderProcessDao;


    public PinOnPnServiceImpl(final JsonUtil jsonUtil,
                              final DateUtil dateUtil,
                              final TenderProcessDao tenderProcessDao) {
        this.jsonUtil = jsonUtil;
        this.dateUtil = dateUtil;
        this.tenderProcessDao = tenderProcessDao;
    }

    @Override
    public ResponseDto createPinOnPn(final String cpId,
                                     final String token,
                                     final String owner,
                                     final String stage,
                                     final String previousStage,
                                     final LocalDateTime dateTime,
                                     final PinProcess pin) {

        final TenderProcessEntity entity = Optional.ofNullable(tenderProcessDao.getByCpIdAndStage(cpId, previousStage))
                .orElseThrow(() -> new ErrorException(ErrorType.DATA_NOT_FOUND));
        if (!entity.getOwner().equals(owner))
            throw new ErrorException(ErrorType.INVALID_OWNER);
        if (!entity.getToken().toString().equals(token))
            throw new ErrorException(ErrorType.INVALID_TOKEN);
        if (!entity.getCpId().equals(pin.getTender().getId()))
            throw new ErrorException(ErrorType.INVALID_CPID_FROM_DTO);
        final PnProcess pn = jsonUtil.toObject(PnProcess.class, entity.getJsonData());
        final PnTender pnTender = pn.getTender();
        validateLots(pn, pin);
        pin.setPlanning(pn.getPlanning());
        final PinTender pinTender = pin.getTender();
        pinTender.setTitle(pnTender.getTitle());
        pinTender.setDescription(pnTender.getDescription());
        pinTender.setClassification(pnTender.getClassification());
        pinTender.setLegalBasis(pnTender.getLegalBasis());
        pinTender.setProcurementMethod(pnTender.getProcurementMethod());
        pinTender.setProcurementMethodDetails(pnTender.getProcurementMethodDetails());
        pinTender.setMainProcurementCategory(pnTender.getMainProcurementCategory());
        pinTender.setProcuringEntity(pnTender.getProcuringEntity());
        setTenderStatus(pinTender);
        setLotsStatus(pinTender);
        tenderProcessDao.save(getEntity(pin, cpId, stage, entity.getToken(), dateTime, owner));
        pin.setOcId(cpId);
        pin.setToken(entity.getToken().toString());
        return new ResponseDto<>(true, null, pin);
    }

    private void validateLots(final PnProcess pn, final PinProcess pin) {
        Set<String> lotsFromDocuments = null;
        if (pin.getTender().getDocuments() != null) {
            lotsFromDocuments = pin.getTender().getDocuments().stream()
                    .flatMap(d -> d.getRelatedLots().stream()).collect(Collectors.toSet());
        }
        // validate lots from pn
        if (pn.getTender().getLots() != null) {
            if (lotsFromDocuments != null) {
                final Set<String> lotsFromPn = pn.getTender().getLots().stream().map(PnLot::getId).collect(Collectors.toSet());
                if (!lotsFromPn.containsAll(lotsFromDocuments))
                    throw new ErrorException(ErrorType.INVALID_LOTS_RELATED_LOTS);
            }
            addLotsToPinFromPn(pn, pin);
        }
        //validate lots from pin
        else {
            if (pin.getTender().getLots() != null && lotsFromDocuments != null) {
                final Set<String> lotsFromPin = pin.getTender().getLots().stream().map(PinLot::getId).collect(Collectors.toSet());
                if (!lotsFromPin.containsAll(lotsFromDocuments))
                    throw new ErrorException(ErrorType.INVALID_LOTS_RELATED_LOTS);
            }
        }
    }

    private void addLotsToPinFromPn(final PnProcess pn, final PinProcess pin) {
        final List<PinLot> pinLots = pn.getTender().getLots().stream()
                .map(this::convertPnToPinLot)
                .collect(Collectors.toList());
        pin.getTender().setLots(pinLots);
    }

    private PinLot convertPnToPinLot(final PnLot pnLot) {
        return new PinLot(
                pnLot.getId(),
                pnLot.getTitle(),
                pnLot.getDescription(),
                pnLot.getStatus(),
                pnLot.getStatusDetails(),
                pnLot.getValue(),
                pnLot.getOptions(),
                pnLot.getRecurrentProcurement(),
                pnLot.getRenewals(),
                pnLot.getVariants(),
                pnLot.getContractPeriod(),
                pnLot.getPlaceOfPerformance()
        );
    }

    private void setTenderStatus(final PinTender tender) {
        tender.setStatus(PLANNED);
        tender.setStatusDetails(EMPTY);
    }

    private void setLotsStatus(final PinTender tender) {
        if (tender.getLots() != null) {
            tender.getLots().forEach(lot -> {
                lot.setStatus(PLANNED);
                lot.setStatusDetails(EMPTY);
            });
        }
    }

    private TenderProcessEntity getEntity(final PinProcess pin,
                                          final String cpId,
                                          final String stage,
                                          final UUID token,
                                          final LocalDateTime dateTime,
                                          final String owner) {
        final TenderProcessEntity entity = new TenderProcessEntity();
        entity.setCpId(cpId);
        entity.setToken(token);
        entity.setStage(stage);
        entity.setOwner(owner);
        entity.setCreatedDate(dateUtil.localToDate(dateTime));
        entity.setJsonData(jsonUtil.toJson(pin));
        return entity;
    }


}
