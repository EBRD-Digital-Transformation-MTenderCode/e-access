package com.procurement.access.service;

import com.procurement.access.config.properties.OCDSProperties;
import com.procurement.access.model.dto.bpe.ResponseDetailsDto;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.cn.CnDto;
import com.procurement.access.model.entity.CnEntity;
import com.procurement.access.repository.CnRepository;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

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
    public ResponseDto createCn(CnDto cnDto) {
        final LocalDateTime addedDate = dateUtil.getNowUTC();
        final long timeStamp = dateUtil.getMilliUTC(addedDate);
        final String ocId = getOcId(cnDto, timeStamp);
        cnDto.getTender().setId(ocId);
        cnRepository.save(getEntity(cnDto));
        return getResponseDto(cnDto);
    }

    private String getOcId(final CnDto cnDto, final long timeStamp) {
        final String ocId;
        if (Objects.isNull(cnDto.getTender().getId())) {
            ocId = ocdsProperties.getPrefix() + timeStamp;
            cnDto.getTender().setId(ocId);
        } else {
            ocId = cnDto.getTender().getId();
        }
        return ocId;
    }

    private CnEntity getEntity(final CnDto cnDto) {
        final CnEntity cnEntity = new CnEntity();
        cnEntity.setOcId(cnDto.getTender().getId());
        cnEntity.setJsonData(jsonUtil.toJson(cnDto.getTender()));
        return cnEntity;
    }

    private ResponseDto getResponseDto(final CnDto cnDto) {
        final ResponseDetailsDto details = new ResponseDetailsDto(HttpStatus.OK.toString(), "ok");
        return new ResponseDto(true, Collections.singletonList(details), cnDto);
    }

}
