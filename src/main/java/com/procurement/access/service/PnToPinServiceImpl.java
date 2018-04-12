package com.procurement.access.service;

import com.procurement.access.dao.TenderProcessDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.exception.ErrorType;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ocds.Lot;
import com.procurement.access.model.dto.pn.PnDto;
import com.procurement.access.model.dto.pnToPin.PnToPinDto;
import com.procurement.access.model.dto.tender.TenderProcessDto;
import com.procurement.access.model.entity.TenderProcessEntity;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PnToPinServiceImpl implements PnToPinService {

    private final JsonUtil jsonUtil;
    private final DateUtil dateUtil;
    private final TenderProcessDao tenderProcessDao;

    public PnToPinServiceImpl(JsonUtil jsonUtil,
                              DateUtil dateUtil,
                              TenderProcessDao tenderProcessDao) {
        this.jsonUtil = jsonUtil;
        this.dateUtil = dateUtil;
        this.tenderProcessDao = tenderProcessDao;
    }

    @Override
    public ResponseDto createPinfromPn(String id,
                                       String previosStage,
                                       String stage,
                                       String owner,
                                       String token,
                                       LocalDateTime dateTime,
                                       PnToPinDto data) {
        final TenderProcessEntity entity = Optional.ofNullable(
            tenderProcessDao.getByCpIdAndTokenAndStage(id, UUID.fromString(token), stage))
                                                   .orElseThrow(() -> new ErrorException(ErrorType.DATA_NOT_FOUND));

        if (!entity.getOwner().equals(owner))
            throw new ErrorException(ErrorType.INVALID_OWNER);

        if(!entity.getToken().toString().equals(token))
            throw new ErrorException(ErrorType.INVALID_TOKEN);

        if (!entity.getCpId().equals(data.getTender().getId()))
            throw new ErrorException(ErrorType.INVALID_CPID_FROM_DTO);



        TenderProcessDto tenderProcessDto = jsonUtil.toObject(TenderProcessDto.class, entity.getJsonData());
        validateLots(tenderProcessDto,data);






        return null;
    }

    private void validateLots(TenderProcessDto tenderProcessDto,PnToPinDto pnToPinDto){
        Set<String> lotsIdsFromDocuments = new HashSet<>();

        for (int i = 0; i < pnToPinDto.getTender().getDocuments().size(); i++) {
            lotsIdsFromDocuments.addAll(pnToPinDto.getTender().getDocuments().get(i).getRelatedLots());
        }


        List<Lot> lotsPn = tenderProcessDto.getTender().getLots();

        Set<String> lotsIds = new HashSet<>();

        if (lotsPn.isEmpty()){
            for (int i = 0; i < pnToPinDto.getTender().getLots().size(); i++) {
                lotsIds.add(pnToPinDto.getTender().getLots().get(i).getId());
            }

        }else {
            for (int i = 0; i < tenderProcessDto.getTender().getLots().size(); i++) {
                lotsIds.add(tenderProcessDto.getTender().getLots().get(i).getId());
            }

        }

        if(!lotsIdsFromDocuments.isEmpty()){
            if(!lotsIdsFromDocuments.containsAll(lotsIds)){
                throw new ErrorException(ErrorType.INVALID_LOTS_RELATED_LOTS);
            }
        }
    }





}
