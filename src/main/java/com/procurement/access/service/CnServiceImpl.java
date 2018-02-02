package com.procurement.access.service;

import com.datastax.driver.core.utils.UUIDs;
import com.procurement.access.config.properties.OCDSProperties;
import com.procurement.access.dao.CnDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.cn.CnDto;
import com.procurement.access.model.dto.cn.CnResponseDto;
import com.procurement.access.model.dto.ocds.RelatedProcess;
import com.procurement.access.model.entity.CnEntity;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;

import static com.procurement.access.model.dto.ocds.TenderStatus.ACTIVE;

@Service
public class CnServiceImpl implements CnService {

    private final OCDSProperties ocdsProperties;
    private final JsonUtil jsonUtil;
    private final DateUtil dateUtil;
    private final CnDao cnDao;

    public CnServiceImpl(final OCDSProperties ocdsProperties,
                         final JsonUtil jsonUtil,
                         final DateUtil dateUtil,
                         final CnDao cnDao) {
        this.ocdsProperties = ocdsProperties;
        this.jsonUtil = jsonUtil;
        this.dateUtil = dateUtil;
        this.cnDao = cnDao;
    }

    @Override
    public ResponseDto createCn(final String owner,
                                final CnDto cn) {
        setTenderId(cn);
        setItemsId(cn);
        setLotsIdAndItemsRelatedLots(cn);
        setTenderStatus(cn);
        checkAmount(cn);
        final CnEntity entity = getEntity(cn, owner);
        cnDao.save(entity);
        return getResponseDto(entity.getCpId(), entity.getToken(), cn);
    }

    @Override
    public ResponseDto updateCn(final String owner,
                                final String cpId,
                                final String token,
                                final CnDto cnDto) {
        final CnEntity entity = Optional.ofNullable(cnDao.getByCpIdAndToken(cpId, token))
                .orElseThrow(() -> new ErrorException("Data not found."));
        if (!entity.getOwner().equals(owner)) throw new ErrorException("Invalid owner.");
        final CnDto cn = jsonUtil.toObject(CnDto.class, entity.getJsonData());
        cn.setTender(cnDto.getTender());
        entity.setJsonData(jsonUtil.toJson(cn));
        cnDao.save(entity);
        return getResponseDto(cpId, entity.getToken(), cn);
    }


    private void setTenderId(final CnDto cn) {
        if (Objects.isNull(cn.getTender().getId())) {
            cn.getTender().setId(ocdsProperties.getPrefix() + dateUtil.getMilliNowUTC());
        }
    }

    private void setTenderStatus(final CnDto cn) {
        cn.getTender().setStatus(ACTIVE);
    }

    private void setItemsId(final CnDto cnDto) {
        cnDto.getTender().getItems().forEach(i -> {
            i.setId(UUIDs.timeBased().toString());
        });
    }

    private void checkAmount(final CnDto cn) {
    }

    private void setLotsIdAndItemsRelatedLots(final CnDto cn) {
        cn.getTender().getLots().forEach(l -> {
            final String id = UUIDs.timeBased().toString();
            cn.getTender().getItems()
                    .stream()
                    .filter(i -> i.getRelatedLot().equals(l.getId()))
                    .findFirst()
                    .get()
                    .setRelatedLot(id);

            l.setId(id);
        });
    }

    private RelatedProcess getFsRelatedProcess(final CnDto cn) {
        return cn.getRelatedProcesses()
                .stream()
                .filter(rp -> rp.getRelationship().contains(RelatedProcess.RelatedProcessType.X_BUDGET))
                .filter(rp -> rp.getScheme().equals(RelatedProcess.RelatedProcessScheme.OCID))
                .filter(rp -> !rp.getIdentifier().isEmpty())
                .findFirst().orElse(null);
    }

    private CnEntity getEntity(final CnDto cn, final String owner) {
        final CnEntity entity = new CnEntity();
        entity.setCpId(cn.getTender().getId());
        entity.setToken(UUIDs.timeBased().toString());
        entity.setOwner(owner);
        entity.setJsonData(jsonUtil.toJson(cn));
        return entity;
    }

    private ResponseDto getResponseDto(final String cpId, final String token, final CnDto cn) {
        final CnResponseDto responseDto = new CnResponseDto(
                token,
                cpId,
                cn.getPlanning(),
                cn.getTender(),
                cn.getParties(),
                cn.getBuyer(),
                cn.getRelatedProcesses()
        );
        return new ResponseDto(true, null, responseDto);
    }

}
