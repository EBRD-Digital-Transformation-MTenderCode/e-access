package com.procurement.access.service;

import com.datastax.driver.core.utils.UUIDs;
import com.procurement.access.config.properties.OCDSProperties;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ein.EinDto;
import com.procurement.access.model.entity.EinEntity;
import com.procurement.access.repository.EinRepository;
import com.procurement.access.utils.JsonUtil;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class EinServiceImpl implements EinService {

    private final OCDSProperties ocdsProperties;
    private final JsonUtil jsonUtil;
    private final EinRepository einRepository;

    public EinServiceImpl(final OCDSProperties ocdsProperties,
                          final JsonUtil jsonUtil,
                          final EinRepository einRepository) {
        this.ocdsProperties = ocdsProperties;
        this.jsonUtil = jsonUtil;
        this.einRepository = einRepository;
    }

    @Override
    public ResponseDto createEin(EinDto einDto) {
        LocalDateTime addedDate = LocalDateTime.now();
        List<String> tag = Arrays.asList("budget");
        String initiationType = "budget";
        einDto.setDate(addedDate);
        einDto.setTag(tag);
        einDto.setInitiationType(initiationType);
        einRepository.save(getEntity(einDto));
        return getResponseDto(einDto);
    }

    private EinEntity getEntity(final EinDto einDto) {
        EinEntity einEntity = new EinEntity();
        einEntity.setOcId(getOcId(einDto));
        einEntity.setEinId(getUuid(einDto));
        einEntity.setJsonData(jsonUtil.toJson(einDto));
        return einEntity;
    }

    private UUID getUuid(final EinDto einDto) {
        UUID einId;
        if (Objects.isNull(einDto.getId())) {
            einId = UUIDs.timeBased();
            einDto.setId(einId.toString());
        } else {
            einId = UUID.fromString(einDto.getId());
        }
        return einId;
    }
    private String getOcId(final EinDto einDto) {
        String osId;
        if (Objects.isNull(einDto.getOcid())) {
            osId = ocdsProperties.getPrefix() + "-" + einDto.getDate().toInstant(ZoneOffset.UTC).toEpochMilli();
            einDto.setOcid(osId);
        } else {
            osId = einDto.getOcid();
        }
        return osId;
    }

    private ResponseDto getResponseDto(final EinDto einDto){
        Map<String, String> data = new HashMap<>();
        data.put("ocid", einDto.getOcid());
        data.put("ein", jsonUtil.toJson(einDto));
        ResponseDto.ResponseDetailsDto details = new ResponseDto.ResponseDetailsDto(HttpStatus.OK.toString(), "ok");
        ResponseDto responseDto = new ResponseDto(true, Collections.singletonList(details), data);
        return responseDto;
    }
}
