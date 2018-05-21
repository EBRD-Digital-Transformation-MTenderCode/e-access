package com.procurement.access.service;

import com.procurement.access.model.dto.cn.CnLot;
import com.procurement.access.model.dto.cn.CnProcess;
import com.procurement.access.model.dto.cn.CnTender;
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

        if (!pinTender.getTenderPeriod().getStartDate().toLocalDate().equals(dateTime.toLocalDate()))
            throw new ErrorException(ErrorType.INVALID_START_DATE);

        /*planning*/
        cn.setPlanning(pin.getPlanning());
        /*tender*/
        final CnTender cnTender = convertPinToCnTender(pinTender);
        /*lots*/
        setLotsToCnFromPin(pinTender, cnTender);
         /*tender status, lot status*/
        setStatuses(cnTender);
        /*submissionLanguages*/
        if (cn.getTender().getSubmissionLanguages() != null)
            cnTender.setSubmissionLanguages(cn.getTender().getSubmissionLanguages());
        /*documents*/
        if (cn.getTender().getDocuments() != null)
            cnTender.setDocuments(cn.getTender().getDocuments());
        cn.setTender(cnTender);
        validateLots(cnTender);
        tenderProcessDao.save(getEntity(cn, cpId, stage, entity.getToken(), dateTime, owner));
        cn.setOcId(cpId);
        cn.setToken(entity.getToken().toString());
        return new ResponseDto<>(true, null, cn);
    }

    private void setLotsToCnFromPin(final PinTender pinTender, final CnTender cnTender) {
        if (pinTender.getLots() != null) {
            final List<CnLot> cnLots = pinTender.getLots().stream()
                    .map(this::convertPinToCnLot)
                    .collect(Collectors.toList());
            cnTender.setLots(cnLots);
        }
    }

    private void validateLots(final CnTender cnTender) {
        Set<String> lotsFromDocuments = null;
        if (cnTender.getDocuments() != null) {
            lotsFromDocuments = cnTender.getDocuments().stream()
                    .filter(document -> Objects.nonNull(document.getRelatedLots()))
                    .flatMap(d -> d.getRelatedLots().stream()).collect(Collectors.toSet());
        }

        if (cnTender.getLots() != null && lotsFromDocuments != null && !lotsFromDocuments.isEmpty()) {
            final Set<String> lotsFromCn = cnTender.getLots().stream().map(CnLot::getId).collect(Collectors.toSet());
            if (!lotsFromCn.containsAll(lotsFromDocuments))
                throw new ErrorException(ErrorType.INVALID_LOTS_RELATED_LOTS);
        }
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
        if (cnTender.getLots() != null)
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
