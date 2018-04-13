package com.procurement.access.service;

import com.procurement.access.dao.TenderProcessDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.exception.ErrorType;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ocds.LegalBasis;
import com.procurement.access.model.dto.ocds.MainProcurementCategory;
import com.procurement.access.model.dto.ocds.ProcurementMethod;
import com.procurement.access.model.dto.pin.PinLot;
import com.procurement.access.model.dto.pin.PinProcess;
import com.procurement.access.model.dto.pin.PinTender;
import com.procurement.access.model.dto.pn.PnLot;
import com.procurement.access.model.dto.pn.PnProcess;
import com.procurement.access.model.dto.pn.PnTender;
import com.procurement.access.model.entity.TenderProcessEntity;
import com.procurement.access.utils.JsonUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PinOnPnServiceImpl implements PinOnPnService {

    private final JsonUtil jsonUtil;
    private final TenderProcessDao tenderProcessDao;

    public PinOnPnServiceImpl(JsonUtil jsonUtil,
                              TenderProcessDao tenderProcessDao) {
        this.jsonUtil = jsonUtil;
        this.tenderProcessDao = tenderProcessDao;
    }

    @Override
    public ResponseDto createPinOnPn(String cpId, String token, String owner, String stage, String previousStage, LocalDateTime dateTime, PinProcess pin) {

        final TenderProcessEntity entity = Optional.ofNullable(tenderProcessDao.getByCpIdAndStage(cpId, stage))
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
        pinTender.setLegalBasis(LegalBasis.fromValue(pnTender.getLegalBasis().toString()));
        pinTender.setProcurementMethod(ProcurementMethod.fromValue(pnTender.getProcurementMethod().toString()));
        pinTender.setProcurementMethodDetails(pnTender.getProcurementMethodDetails());
        pinTender.setMainProcurementCategory(MainProcurementCategory.fromValue(pnTender.getMainProcurementCategory().toString()));
        pinTender.setProcuringEntity(pnTender.getProcuringEntity());

        return new ResponseDto<>(true, null, pin);
    }

    private void validateLots(final PnProcess pn, final PinProcess pin) {
        final Set<String> lotsFromDocuments = pin.getTender().getDocuments().stream()
                .flatMap(d -> d.getRelatedLots().stream()).collect(Collectors.toSet());
        // validate lots from pn
        if (pn.getTender().getLots() != null) {
            final Set<String> lotsFromPn = pn.getTender().getLots().stream().map(PnLot::getId).collect(Collectors.toSet());
            if (!lotsFromPn.containsAll(lotsFromDocuments))
                throw new ErrorException(ErrorType.INVALID_LOTS_RELATED_LOTS);
            addLotsToPinFromPn(pn, pin);
        }
        //validate lots from pin
        else {
            final Set<String> lotsFromPin = pin.getTender().getLots().stream().map(PinLot::getId).collect(Collectors.toSet());
            if (!lotsFromPin.containsAll(lotsFromDocuments))
                throw new ErrorException(ErrorType.INVALID_LOTS_RELATED_LOTS);
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
}
