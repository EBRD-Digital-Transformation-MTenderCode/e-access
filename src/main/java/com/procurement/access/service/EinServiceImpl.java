package com.procurement.access.service;

import com.datastax.driver.core.utils.UUIDs;
import com.procurement.access.config.properties.OCDSProperties;
import com.procurement.access.model.dto.bpe.ResponseDetailsDto;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ein.EinDto;
import com.procurement.access.model.dto.ein.EinRelatedProcessDto;
import com.procurement.access.model.dto.ein.EinResponseDto;
import com.procurement.access.model.dto.ein.UpdateFsDto;
import com.procurement.access.model.entity.EinEntity;
import com.procurement.access.repository.EinRepository;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class EinServiceImpl implements EinService {

    private final OCDSProperties ocdsProperties;
    private final JsonUtil jsonUtil;
    private final DateUtil dateUtil;
    private final EinRepository einRepository;
    private final FsService fsService;

    public EinServiceImpl(final OCDSProperties ocdsProperties,
                          final JsonUtil jsonUtil,
                          final DateUtil dateUtil,
                          final EinRepository einRepository,
                          final FsService fsService) {
        this.ocdsProperties = ocdsProperties;
        this.jsonUtil = jsonUtil;
        this.dateUtil = dateUtil;
        this.einRepository = einRepository;
        this.fsService = fsService;
    }

    @Override
    public ResponseDto createEin(final EinDto einDto) {
        final LocalDateTime addedDate = dateUtil.getNowUTC();
        final long timeStamp = dateUtil.getMilliUTC(addedDate);
        einDto.setDate(addedDate);
        einDto.setTag(Arrays.asList("compiled"));
        einDto.setInitiationType("tender");
        einDto.setLanguage("en");
        final String ocId = getOcId(einDto, timeStamp);
        einDto.getTender()
              .setId(ocId);
        einDto.setId(getId(einDto, timeStamp));
        einDto.getPlanning()
              .getBudget()
              .setId(UUIDs.timeBased()
                          .toString());
        einRepository.save(getEntity(einDto, addedDate));
        return getResponseDto(einDto);
    }

    @Override
    public ResponseDto updateEin(final EinDto einDto) {
        return null;
    }

    @Override
    public ResponseDto addRelatedProcess(final UpdateFsDto updateFsDto) {
        final EinEntity einEntity = einRepository.getLastByOcId(updateFsDto.getCpId());
        final EinDto einDto = jsonUtil.toObject(EinDto.class, einEntity.getJsonData());
        addFsRelatedProcessToEin(einDto, updateFsDto.getOcId());
        final LocalDateTime addedDate = dateUtil.getNowUTC();
        final long timeStamp = dateUtil.getMilliUTC(addedDate);
        einDto.setDate(addedDate);
        einDto.setId(getId(einDto, timeStamp));
//        final Double totalAmount = fsService.getTotalAmountFs(updateFsDto.getCpId());
//        einDto.getPlanning().getBudget().getAmount().setAmount(totalAmount);
        einRepository.save(getEntity(einDto, addedDate));
        return getResponseDto(einDto);
    }

    private String getId(final EinDto einDto, final long timeStamp) {
        return einDto.getOcId() + "-EIN-" + timeStamp;
    }

    private String getOcId(final EinDto einDto, final long timeStamp) {
        final String ocId;
        if (Objects.isNull(einDto.getOcId())) {
            ocId = ocdsProperties.getPrefix() + timeStamp;
            einDto.setOcId(ocId);
        } else {
            ocId = einDto.getOcId();
        }
        return ocId;
    }

    private EinEntity getEntity(final EinDto einDto, final LocalDateTime addedDate) {
        final EinEntity einEntity = new EinEntity();
        einEntity.setDate(addedDate);
        einEntity.setOcId(einDto.getOcId());
        einEntity.setJsonData(jsonUtil.toJson(einDto));
        return einEntity;
    }

    private void addFsRelatedProcessToEin(final EinDto einDto, final String ocId) {
        final EinRelatedProcessDto relatedProcessDto = new EinRelatedProcessDto();
        relatedProcessDto.setId(UUIDs.timeBased()
                                     .toString());
        relatedProcessDto.setRelationship(Arrays.asList(EinRelatedProcessDto.RelatedProcessType.FRAMEWORK));
        relatedProcessDto.setScheme(EinRelatedProcessDto.RelatedProcessScheme.OCID);
        relatedProcessDto.setIdentifier(ocId);
        final List<EinRelatedProcessDto> relatedProcesses = einDto.getRelatedProcesses();
        relatedProcesses.add(relatedProcessDto);
        einDto.setRelatedProcesses(relatedProcesses);
    }

    private ResponseDto getResponseDto(final EinDto einDto) {
        final EinResponseDto einResponseDto = new EinResponseDto();
        einResponseDto.setCpId(einDto.getOcId());
        einResponseDto.setOcId(einDto.getOcId());
        einResponseDto.setReleaseDate(einDto.getDate());
        einResponseDto.setReleaseId(einDto.getId());
        einResponseDto.setJsonData(einDto);
        final ResponseDetailsDto details = new ResponseDetailsDto(HttpStatus.OK.toString(), "ok");
        return new ResponseDto(true, Collections.singletonList(details), einResponseDto);
    }
}
