package com.procurement.access.service;

import com.datastax.driver.core.utils.UUIDs;
import com.procurement.access.config.properties.OCDSProperties;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.cn.CnDto;
import com.procurement.access.model.dto.cn.CnResponseDto;
import com.procurement.access.model.dto.cn.CnTenderStatusDto;
import com.procurement.access.model.entity.CnEntity;
import com.procurement.access.repository.CnRepository;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class CnServiceImpl implements CnService {

    private final OCDSProperties ocdsProperties;
    private final JsonUtil jsonUtil;
    private final DateUtil dateUtil;
    private final CnRepository cnRepository;

    public CnServiceImpl(final OCDSProperties ocdsProperties,
                         final JsonUtil jsonUtil,
                         final DateUtil dateUtil,
                         final CnRepository cnRepository) {
        this.ocdsProperties = ocdsProperties;
        this.jsonUtil = jsonUtil;
        this.dateUtil = dateUtil;
        this.cnRepository = cnRepository;
    }

    @Override
    public ResponseDto createCn(final String country,
                                final String pmd,
                                final String stage,
                                final String owner,
                                final CnDto cn) {
        setTenderId(cn);
        setItemsId(cn);
        setLotsIdAndItemsRelatedLots(cn);
        setTenderStatus(cn);
        final CnEntity entity = cnRepository.save(getEntity(cn, owner));
        return getResponseDto(cn, entity);
    }

    private void setTenderId(final CnDto cn) {
        if (Objects.isNull(cn.getTender().getId())) {
            cn.getTender().setId(ocdsProperties.getPrefix() + dateUtil.getMilliNowUTC());
        }
    }

    private void setTenderStatus(final CnDto cn) {
        cn.getTender().setStatus(CnTenderStatusDto.ACTIVE);
    }

    private void setItemsId(final CnDto cnDto) {
        cnDto.getTender().getItems().forEach(i -> {
            i.setId(UUIDs.timeBased().toString());
        });
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

    private CnEntity getEntity(final CnDto cn, final String owner) {
        final CnEntity entity = new CnEntity();
        entity.setCpId(cn.getTender().getId());
        entity.setToken(UUIDs.timeBased().toString());
        entity.setOwner(owner);
        entity.setJsonData(jsonUtil.toJson(cn));
        return entity;
    }

    private ResponseDto getResponseDto(final CnDto cn, final CnEntity entity) {
        final CnResponseDto responseDto = new CnResponseDto(
                entity.getToken(),
                entity.getCpId(),
                cn.getPlanning(),
                cn.getTender(),
                cn.getParties(),
                cn.getBuyer(),
                cn.getRelatedProcesses()
        );
        return new ResponseDto(true, null, responseDto);
    }

}
