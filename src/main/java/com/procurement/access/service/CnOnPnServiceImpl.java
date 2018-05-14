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
import com.procurement.access.model.dto.pn.PnLot;
import com.procurement.access.model.dto.pn.PnProcess;
import com.procurement.access.model.dto.pn.PnTender;
import com.procurement.access.model.entity.TenderProcessEntity;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CnOnPnServiceImpl implements CnOnPnService {
    private final JsonUtil jsonUtil;
    private final DateUtil dateUtil;
    private final TenderProcessDao tenderProcessDao;


    public CnOnPnServiceImpl(final JsonUtil jsonUtil,
                             final DateUtil dateUtil,
                             final TenderProcessDao tenderProcessDao) {
        this.jsonUtil = jsonUtil;
        this.dateUtil = dateUtil;
        this.tenderProcessDao = tenderProcessDao;
    }

    @Override
    public ResponseDto createCnOnPn(final String cpId,
                                    final String previousStage,
                                    final String stage,
                                    final String owner,
                                    final String token,
                                    final LocalDateTime dateTime,
                                    final CnProcess cn) {

        if (cn.getTender().getEligibilityCriteria() == null) throw new ErrorException(ErrorType.EL_CRITERIA_IS_NULL);

        final TenderProcessEntity entity = Optional.ofNullable(tenderProcessDao.getByCpIdAndStage(cpId, previousStage))
                .orElseThrow(() -> new ErrorException(ErrorType.DATA_NOT_FOUND));
        if (!entity.getOwner().equals(owner))
            throw new ErrorException(ErrorType.INVALID_OWNER);
        if (!entity.getToken().toString().equals(token))
            throw new ErrorException(ErrorType.INVALID_TOKEN);
        if (!entity.getCpId().equals(cn.getTender().getId()))
            throw new ErrorException(ErrorType.INVALID_CPID_FROM_DTO);
        final PnProcess pn = jsonUtil.toObject(PnProcess.class, entity.getJsonData());
        final PnTender pnTender = pn.getTender();
        cn.setPlanning(pn.getPlanning());
        final CnTender cnTender = cn.getTender();
        setLotsToCnFromPn(pnTender, cnTender);
        validateLots(cnTender);
        cnTender.setTitle(pnTender.getTitle());
        cnTender.setDescription(pnTender.getDescription());
        cnTender.setClassification(pnTender.getClassification());
        cnTender.setLegalBasis(pnTender.getLegalBasis());
        cnTender.setProcurementMethod(pnTender.getProcurementMethod());
        cnTender.setProcurementMethodDetails(pnTender.getProcurementMethodDetails());
        cnTender.setMainProcurementCategory(pnTender.getMainProcurementCategory());
        cnTender.setProcuringEntity(pnTender.getProcuringEntity());
        setStatuses(cnTender);
        tenderProcessDao.save(getEntity(cn, cpId, stage, entity.getToken(), dateTime, owner));
        cn.setOcId(cpId);
        cn.setToken(entity.getToken().toString());
        return new ResponseDto<>(true, null, cn);
    }

    private void setLotsToCnFromPn(final PnTender pnTender, final CnTender cnTender) {
        if (pnTender.getLots() != null) {
            final List<CnLot> cnLots = pnTender.getLots().stream()
                    .map(this::convertPnToCnLot)
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

    private CnLot convertPnToCnLot(final PnLot pnLot) {
        return new CnLot(
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

    private void setStatuses(final CnTender cnTender) {
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
