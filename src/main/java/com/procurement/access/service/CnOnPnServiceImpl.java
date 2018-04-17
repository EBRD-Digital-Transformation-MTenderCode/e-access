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
import com.procurement.access.model.dto.pn.PnProcess;
import com.procurement.access.model.dto.pn.PnTender;
import com.procurement.access.model.entity.TenderProcessEntity;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
    public ResponseDto createCnOnPn(String cpId,
                                    String token,
                                    String owner,
                                    String stage,
                                    String previousStage,
                                    LocalDateTime dateTime,
                                    CnProcess cn) {

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
        validateLots(pn, cn);
        cn.setPlanning(pn.getPlanning());
        final CnTender cnTender = cn.getTender();
        cnTender.setTitle(pnTender.getTitle());
        cnTender.setDescription(pnTender.getDescription());
        cnTender.setClassification(pnTender.getClassification());
        cnTender.setLegalBasis(pnTender.getLegalBasis());
        cnTender.setProcurementMethod(pnTender.getProcurementMethod());
        cnTender.setProcurementMethodDetails(pnTender.getProcurementMethodDetails());
        cnTender.setMainProcurementCategory(pnTender.getMainProcurementCategory());
        cnTender.setProcuringEntity(pnTender.getProcuringEntity());
        setStatuses(cnTender);
        tenderProcessDao.save(getEntity(cn, stage, entity.getToken(), dateTime, owner));
        cn.setOcId(cpId);
        cn.setToken(entity.getToken().toString());
        return new ResponseDto<>(true, null, cn);
    }

    private void validateLots(final PnProcess pn, final CnProcess cn) {
        final Set<String> lotsFromDocuments = cn.getTender().getDocuments().stream()
                .flatMap(d -> d.getRelatedLots().stream()).collect(Collectors.toSet());
        // validate lots from pn
        if (pn.getTender().getLots() != null) {
            final Set<String> lotsFromPn = pn.getTender().getLots().stream().map(CnLot::getId).collect(Collectors.toSet());
            if (!lotsFromPn.containsAll(lotsFromDocuments))
                throw new ErrorException(ErrorType.INVALID_LOTS_RELATED_LOTS);
            addLotsToCnFromPn(pn, cn);
        }
        //validate lots from cn
        else {
            final Set<String> lotsFromPin = cn.getTender().getLots().stream().map(CnLot::getId).collect(Collectors.toSet());
            if (!lotsFromPin.containsAll(lotsFromDocuments))
                throw new ErrorException(ErrorType.INVALID_LOTS_RELATED_LOTS);
        }
    }

    private void addLotsToCnFromPn(final PnProcess pn, final CnProcess cn) {
        final List<CnLot> cnLots = pn.getTender().getLots();
        cn.getTender().setLots(cnLots);
    }

    private TenderProcessEntity getEntity(final CnProcess cn,
                                          final String stage,
                                          final UUID token,
                                          final LocalDateTime dateTime,
                                          final String owner) {
        final TenderProcessEntity entity = new TenderProcessEntity();
        entity.setCpId(cn.getTender().getId());
        entity.setToken(token);
        entity.setStage(stage);
        entity.setOwner(owner);
        entity.setCreatedDate(dateUtil.localToDate(dateTime));
        entity.setJsonData(jsonUtil.toJson(cn));
        return entity;
    }

    private void setStatuses(CnTender cnTender) {
        cnTender.setStatus(TenderStatus.ACTIVE);
        cnTender.setStatusDetails(TenderStatusDetails.EMPTY);
        for (int i = 0; i < cnTender.getLots().size(); i++) {
            cnTender.getLots().get(i).setStatus(TenderStatus.ACTIVE);
            cnTender.getLots().get(i).setStatusDetails(TenderStatusDetails.EMPTY);
        }

    }
}
