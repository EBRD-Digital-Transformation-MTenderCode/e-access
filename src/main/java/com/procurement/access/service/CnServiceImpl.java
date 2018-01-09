package com.procurement.access.service;

import com.datastax.driver.core.utils.UUIDs;
import com.procurement.access.config.properties.OCDSProperties;
import com.procurement.access.model.dto.bpe.CnResponseDto;
import com.procurement.access.model.dto.bpe.ResponseDetailsDto;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.cn.CnDto;
import com.procurement.access.model.dto.cn.CnTenderStatusDto;
import com.procurement.access.model.entity.CnEntity;
import com.procurement.access.repository.CnRepository;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import java.util.Collections;
import java.util.Objects;
import org.springframework.http.HttpStatus;
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
                                final CnDto cnDto) {
        final long timeStamp = dateUtil.getMilliNowUTC();
        setTenderId(cnDto, timeStamp);
        setItemsId(cnDto);
        setLotsIdAndItemsRelatedLots(cnDto);
        setTenderStatus(cnDto);
        final CnEntity entity = cnRepository.save(getEntity(cnDto, owner));
        return getResponseDto(cnDto, entity);
    }

    private void setTenderId(final CnDto cnDto, final long timeStamp) {
        if (Objects.isNull(cnDto.getTender().getId())) {
            cnDto.getTender().setId(ocdsProperties.getPrefix() + timeStamp);
        }
    }

    private void setTenderStatus(final CnDto cnDto) {
        cnDto.getTender().setStatus(CnTenderStatusDto.ACTIVE);
    }

    private void setItemsId(final CnDto cnDto) {
        cnDto.getTender().getItems().forEach(i -> {
            i.setId(UUIDs.timeBased().toString());
        });
    }

    private void setLotsIdAndItemsRelatedLots(final CnDto cnDto) {
        cnDto.getTender().getLots().forEach(l -> {
            final String id = UUIDs.timeBased().toString();
            cnDto.getTender().getItems()
                    .stream()
                    .filter(i -> i.getRelatedLot().equals(l.getId()))
                    .findFirst()
                    .get()
                    .setRelatedLot(id);

            l.setId(id);
        });
    }

    private CnEntity getEntity(final CnDto cnDto, final String owner) {
        final CnEntity entity = new CnEntity();
        entity.setCpId(cnDto.getTender().getId());
        entity.setTokenEntity(UUIDs.timeBased().toString());
        entity.setOwner(owner);
        entity.setJsonData(jsonUtil.toJson(cnDto));
        return entity;
    }

    private ResponseDto getResponseDto(final CnDto cnDto, final CnEntity entity) {
        final CnResponseDto responseDto = new CnResponseDto(
                entity.getCpId(),
                entity.getTokenEntity(),
                cnDto.getPlanning(),
                cnDto.getTender(),
                cnDto.getParties(),
                cnDto.getBuyer(),
                cnDto.getRelatedProcesses()
        );
        final ResponseDetailsDto details = new ResponseDetailsDto(HttpStatus.CREATED.toString(), "ok");
        return new ResponseDto(true, Collections.singletonList(details), responseDto);
    }

}
