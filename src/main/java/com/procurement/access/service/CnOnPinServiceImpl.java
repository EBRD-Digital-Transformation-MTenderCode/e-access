package com.procurement.access.service;

import com.procurement.access.dao.TenderProcessDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.exception.ErrorType;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.cn.CnLot;
import com.procurement.access.model.dto.cn.CnProcess;
import com.procurement.access.model.dto.cn.CnTender;
import com.procurement.access.model.dto.ocds.TenderStatus;
import com.procurement.access.model.dto.ocds.TenderStatusDetails;
import com.procurement.access.model.dto.pin.PinLot;
import com.procurement.access.model.dto.pin.PinProcess;
import com.procurement.access.model.dto.pin.PinTender;
import com.procurement.access.model.entity.TenderProcessEntity;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CnOnPinServiceImpl implements CnOnPinService {
    private final JsonUtil jsonUtil;
    private final DateUtil dateUtil;
    private final TenderProcessDao tenderProcessDao;


    public CnOnPinServiceImpl(final JsonUtil jsonUtil,
                              final DateUtil dateUtil,
                              final TenderProcessDao tenderProcessDao) {
        this.jsonUtil = jsonUtil;
        this.dateUtil = dateUtil;
        this.tenderProcessDao = tenderProcessDao;
    }

    @Override
    public ResponseDto createCnOnPin(final String cpId,
                                     final String previousStage,
                                     final String stage,
                                     final String owner,
                                     final String token,
                                     final LocalDateTime dateTime,
                                     final CnProcess cn) {

        final TenderProcessEntity entity = Optional.ofNullable(tenderProcessDao.getByCpIdAndStage(cpId, previousStage))
                .orElseThrow(() -> new ErrorException(ErrorType.DATA_NOT_FOUND));
        if (!entity.getOwner().equals(owner))
            throw new ErrorException(ErrorType.INVALID_OWNER);
        if (!entity.getToken().toString().equals(token))
            throw new ErrorException(ErrorType.INVALID_TOKEN);
        if (!entity.getCpId().equals(cn.getTender().getId()))
            throw new ErrorException(ErrorType.INVALID_CPID_FROM_DTO);
        final PinProcess pin = jsonUtil.toObject(PinProcess.class, entity.getJsonData());
        final PinTender pinTender = pin.getTender();
        /*planning*/
        cn.setPlanning(pin.getPlanning());
        /*tender*/
        final CnTender cnTender = convertPinToCnTender(pinTender);
        /*lots*/
        addLotsToCnFromPin(pin, cn);
        /*tender status, lot status*/
        setStatuses(cnTender);
        /*submissionLanguages*/
        if (cn.getTender().getSubmissionLanguages() != null)
            cnTender.setSubmissionLanguages(cn.getTender().getSubmissionLanguages());
        /*documents*/
        if (cn.getTender().getDocuments() != null)
            cnTender.setDocuments(cn.getTender().getDocuments());
        cn.setTender(cnTender);
        validateLots(cn);
        tenderProcessDao.save(getEntity(cn, cpId, stage, entity.getToken(), dateTime, owner));
        cn.setOcId(cpId);
        cn.setToken(entity.getToken().toString());
        return new ResponseDto<>(true, null, cn);
    }

    private void validateLots(final CnProcess cn) {
        if (cn.getTender().getDocuments() != null) {
            final Set<String> lotsFromDocuments = cn.getTender().getDocuments().stream()
                    .flatMap(d -> d.getRelatedLots().stream()).collect(Collectors.toSet());
            // validate lots from pin
            if (cn.getTender().getLots() != null) {
                final Set<String> lots = cn.getTender().getLots().stream().map(CnLot::getId).collect(Collectors
                        .toSet());
                if (!lots.containsAll(lotsFromDocuments))
                    throw new ErrorException(ErrorType.INVALID_LOTS_RELATED_LOTS);
            }
        }
    }

    private void addLotsToCnFromPin(final PinProcess pin, final CnProcess cn) {
        final List<CnLot> cnLots = pin.getTender().getLots().stream()
                .map(this::convertPinToCnLot)
                .collect(Collectors.toList());
        cn.getTender().setLots(cnLots);
    }

    private CnLot convertPinToCnLot(final PinLot pinLot) {
        return new CnLot(
                pinLot.getId(),
                pinLot.getTitle(),
                pinLot.getDescription(),
                pinLot.getStatus(),
                pinLot.getStatusDetails(),
                pinLot.getValue(),
                pinLot.getOptions(),
                pinLot.getRecurrentProcurement(),
                pinLot.getRenewals(),
                pinLot.getVariants(),
                pinLot.getContractPeriod(),
                pinLot.getPlaceOfPerformance()
        );
    }

    private CnTender convertPinToCnTender(final PinTender pinTender) {
        return new CnTender(
                pinTender.getId(),
                pinTender.getTitle(),
                pinTender.getDescription(),
                pinTender.getStatus(),
                pinTender.getStatusDetails(),
                pinTender.getClassification(),
                new HashSet(pinTender.getItems()),
                pinTender.getValue(),
                pinTender.getProcurementMethod(),
                pinTender.getProcurementMethodDetails(),
                pinTender.getProcurementMethodRationale(),
                pinTender.getMainProcurementCategory(),
                pinTender.getAdditionalProcurementCategories(),
                pinTender.getAwardCriteria(),
                pinTender.getSubmissionMethod(),
                pinTender.getSubmissionMethodDetails(),
                pinTender.getEligibilityCriteria(),
                pinTender.getContractPeriod(),
                pinTender.getProcuringEntity(),
                pinTender.getDocuments(),
                null,
                pinTender.getLotGroups(),
                pinTender.getAcceleratedProcedure(),
                pinTender.getDesignContest(),
                pinTender.getElectronicWorkflows(),
                pinTender.getJointProcurement(),
                pinTender.getLegalBasis(),
                pinTender.getProcedureOutsourcing(),
                pinTender.getProcurementMethodAdditionalInfo(),
                pinTender.getSubmissionLanguages(),
                pinTender.getSubmissionMethodRationale(),
                pinTender.getDynamicPurchasingSystem(),
                pinTender.getFramework(),
                pinTender.getRequiresElectronicCatalogue()
        );
    }

    private void setStatuses(CnTender cnTender) {
        cnTender.setStatus(TenderStatus.ACTIVE);
        cnTender.setStatusDetails(TenderStatusDetails.EMPTY);
        cnTender.getLots().forEach(lot -> {
            lot.setStatus(TenderStatus.ACTIVE);
            lot.setStatusDetails(TenderStatusDetails.EMPTY);
        });
    }

    private TenderProcessEntity getEntity(final CnProcess cn,
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
        entity.setJsonData(jsonUtil.toJson(cn));
        return entity;
    }


}
