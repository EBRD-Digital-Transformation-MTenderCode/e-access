package com.procurement.access.service;

import com.datastax.driver.core.utils.UUIDs;
import com.procurement.access.config.properties.OCDSProperties;
import com.procurement.access.model.dto.bpe.ResponseDetailsDto;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ein.EinValueDto;
import com.procurement.access.model.dto.ein.UpdateFsDto;
import com.procurement.access.model.dto.ein.EinDto;
import com.procurement.access.model.dto.fs.FsDto;
import com.procurement.access.model.entity.EinEntity;
import com.procurement.access.repository.EinRepository;
import com.procurement.access.utils.JsonUtil;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class EinServiceImpl implements EinService {

    private final OCDSProperties ocdsProperties;
    private final JsonUtil jsonUtil;
    private final EinRepository einRepository;
    private final FsService fsService;

    public EinServiceImpl(final OCDSProperties ocdsProperties,
                          final JsonUtil jsonUtil,
                          final EinRepository einRepository,
                          final FsService fsService) {
        this.ocdsProperties = ocdsProperties;
        this.jsonUtil = jsonUtil;
        this.einRepository = einRepository;
        this.fsService = fsService;
    }

    @Override
    public ResponseDto createEin(final EinDto einDto) {
        final LocalDateTime addedDate = LocalDateTime.now();
        einDto.setDate(addedDate);
        einDto.setTag(Arrays.asList("compiled"));
        einDto.setInitiationType("tender");
        einDto.setLanguage("en");
        einRepository.save(getEntity(einDto, addedDate));
        return getResponseDto(einDto);
    }

    @Override
    public ResponseDto updateEin(EinDto einDto) {
        return null;
    }

    @Override
    public ResponseDto updateFs(UpdateFsDto updateFsDto) {
        final EinEntity einEntity = einRepository.getLastByOcId(updateFsDto.getCpId());
        final String mainJson = einEntity.getJsonData();
        final String updateJson = jsonUtil.toJson(updateFsDto);
        final String resultJson = jsonUtil.merge(mainJson, updateJson);
        final EinDto einDto = jsonUtil.toObject(EinDto.class, resultJson);
        final LocalDateTime addedDate = LocalDateTime.now();
        final long timeStamp = addedDate.toInstant(ZoneOffset.UTC).toEpochMilli();
        einDto.setId(einDto.getOcId()+"-EIN-"+timeStamp);
//        final Double totalAmount = fsService.getTotalAmountFs(updateFsDto.getCpId());
//        einDto.getPlanning().getBudget().getAmount().setAmount(totalAmount);
        return getResponseDto(einDto);
    }

    private EinEntity getEntity(final EinDto einDto, final LocalDateTime addedDate) {
        final EinEntity einEntity = new EinEntity();
        einEntity.setDate(addedDate);
        einEntity.setOcId(getOcId(einDto, addedDate));
        einEntity.setJsonData(jsonUtil.toJson(einDto));
        return einEntity;
    }

    private String getOcId(final EinDto einDto, final LocalDateTime addedDate) {
        final String ocId;
        if (Objects.isNull(einDto.getOcId())) {
            long timeStamp = addedDate.toInstant(ZoneOffset.UTC).toEpochMilli();
            ocId = ocdsProperties.getPrefix() + timeStamp;
            einDto.setOcId(ocId);
            einDto.getTender().setId(ocId);
            einDto.setId(ocId+"-EIN-"+timeStamp);
            einDto.getPlanning().getBudget().setId(UUIDs.timeBased().toString());
        } else {
            ocId = einDto.getOcId();
        }
        return ocId;
    }

    private ResponseDto getResponseDto(final EinDto einDto) {
        final ResponseDetailsDto details = new ResponseDetailsDto(HttpStatus.OK.toString(), "ok");
        return new ResponseDto(true, Collections.singletonList(details), einDto);
    }
}
