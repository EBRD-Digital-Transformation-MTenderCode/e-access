package com.procurement.access.service;

import com.procurement.access.exception.ErrorException;
import com.procurement.access.model.dto.bpe.ResponseDetailsDto;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.fs.FsDto;
import com.procurement.access.model.dto.fs.FsRelatedProcessDto;
import com.procurement.access.model.entity.FsEntity;
import com.procurement.access.repository.FsRepository;
import com.procurement.access.utils.JsonUtil;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class FsServiceImpl implements FsService {

    private final JsonUtil jsonUtil;
    private final FsRepository fsRepository;

    public FsServiceImpl(final JsonUtil jsonUtil,
                         final FsRepository fsRepository) {
        this.jsonUtil = jsonUtil;
        this.fsRepository = fsRepository;
    }

    @Override
    public ResponseDto createFs(final FsDto fsDto) {
        final LocalDateTime addedDate = LocalDateTime.now();
        fsDto.setDate(addedDate);
        fsDto.setInitiationType("tender");
        fsDto.setTag(Arrays.asList("compiled"));
        fsDto.setLanguage("en");
        fsRepository.save(getEntity(fsDto, addedDate));
        return getResponseDto(fsDto);
    }

    @Override
    public Double getTotalAmountFs(String cpId) {
        return fsRepository.getTotalAmountByCpId(cpId);
    }

    private String getCpId(final FsDto fsDto) {
        final List<FsRelatedProcessDto> relatedProcesses = fsDto.getRelatedProcesses();
        if (!relatedProcesses.isEmpty()) {
            return relatedProcesses.get(0)
                                   .getIdentifier();
        } else {
            throw new ErrorException("ocid in related processes is empty.");
        }
    }

    private Double getAmount(final FsDto fsDto) {
        return fsDto.getPlanning()
                    .getBudget()
                    .getAmount()
                    .getAmount();
    }

    private String getOcId(final FsDto fsDto, final LocalDateTime addedDate, final String cpId) {
        final String ocId;
        if (Objects.isNull(fsDto.getOcId())) {
            final long timeStamp = addedDate.toInstant(ZoneOffset.UTC)
                                            .toEpochMilli();
            ocId = cpId + "-FS-" + timeStamp;
            fsDto.setOcId(ocId);
            fsDto.setId(ocId + "-" + timeStamp);
        } else {
            ocId = fsDto.getOcId();
        }
        return ocId;
    }

    private FsEntity getEntity(final FsDto fsDto, final LocalDateTime addedDate) {
        final FsEntity fsEntity = new FsEntity();
        final String cpId = getCpId(fsDto);
        fsEntity.setCpId(cpId);
        fsEntity.setOcId(getOcId(fsDto, addedDate, cpId));
        fsEntity.setDate(addedDate);
        fsEntity.setJsonData(jsonUtil.toJson(fsDto));
        fsEntity.setAmount(getAmount(fsDto));
        return fsEntity;
    }

    private ResponseDto getResponseDto(final FsDto fsDto) {
        final ResponseDetailsDto details = new ResponseDetailsDto(HttpStatus.OK.toString(), "ok");
        return new ResponseDto(true, Collections.singletonList(details), fsDto);
    }
}
