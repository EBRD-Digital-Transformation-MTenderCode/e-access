package com.procurement.access.service;

import com.procurement.access.exception.ErrorException;
import com.procurement.access.model.dto.bpe.ResponseDetailsDto;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.fs.FsDto;
import com.procurement.access.model.dto.fs.FsRelatedProcessDto;
import com.procurement.access.model.dto.fs.FsResponseDto;
import com.procurement.access.model.entity.FsEntity;
import com.procurement.access.repository.FsRepository;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class FsServiceImpl implements FsService {

    private final JsonUtil jsonUtil;
    private final DateUtil dateUtil;
    private final FsRepository fsRepository;

    public FsServiceImpl(final JsonUtil jsonUtil,
                         final DateUtil dateUtil,
                         final FsRepository fsRepository) {
        this.jsonUtil = jsonUtil;
        this.dateUtil = dateUtil;
        this.fsRepository = fsRepository;
    }

    @Override
    public ResponseDto createFs(final FsDto fsDto) {
        final LocalDateTime addedDate = dateUtil.getNowUTC();
        final long timeStamp = dateUtil.getMilliUTC(addedDate);
        final String cpId = getIdentifier(fsDto);
        final String ocId = getOcId(fsDto, timeStamp, cpId);
        fsDto.setId(ocId + "-" + timeStamp);
        fsDto.setDate(addedDate);
        fsDto.setInitiationType("tender");
        fsDto.setTag(Arrays.asList("compiled"));
        fsDto.setLanguage("en");
        fsRepository.save(getEntity(cpId, fsDto, addedDate));
        return getResponseDto(cpId, fsDto);
    }

    @Override
    public Double getTotalAmountFs(final String cpId) {
        return fsRepository.getTotalAmountByCpId(cpId);
    }

    private Double getAmount(final FsDto fsDto) {
        return fsDto.getPlanning()
                    .getBudget()
                    .getAmount()
                    .getAmount();
    }

    private String getIdentifier(final FsDto fsDto) {
        final FsRelatedProcessDto relatedProcess = fsDto.getRelatedProcesses()
                                                  .stream()
                                                  .filter(rp -> rp.getScheme()
                                                                  .value()
                                                                  .equals("ocid"))
                                                  .filter(rp -> !rp.getIdentifier()
                                                                   .isEmpty())
                                                  .findFirst()
                                                  .orElse(null);
        if (Objects.isNull(relatedProcess)) {
            throw new ErrorException("Identifier(ocid) in related processes not found.");
        } else {
            return relatedProcess.getIdentifier();
        }
    }

    private String getOcId(final FsDto fsDto, final long timeStamp, final String cpId) {
        final String ocId;
        if (Objects.isNull(fsDto.getOcId())) {
            ocId = cpId + "-FS-" + timeStamp;
            fsDto.setOcId(ocId);
        } else {
            ocId = fsDto.getOcId();
        }
        return ocId;
    }

    private FsEntity getEntity(final String cpId, final FsDto fsDto, final LocalDateTime addedDate) {
        final FsEntity fsEntity = new FsEntity();
        fsEntity.setCpId(cpId);
        fsEntity.setOcId(fsDto.getOcId());
        fsEntity.setDate(addedDate);
        fsEntity.setJsonData(jsonUtil.toJson(fsDto));
        fsEntity.setAmount(getAmount(fsDto));
        return fsEntity;
    }

    private ResponseDto getResponseDto(final String cpId, final FsDto fsDto) {
        final FsResponseDto fsResponseDto = new FsResponseDto();
        fsResponseDto.setCpId(cpId);
        fsResponseDto.setOcId(fsDto.getOcId());
        fsResponseDto.setReleaseDate(fsDto.getDate());
        fsResponseDto.setReleaseId(fsDto.getId());
        fsResponseDto.setJsonData(fsDto);
        final ResponseDetailsDto details = new ResponseDetailsDto(HttpStatus.OK.toString(), "ok");
        return new ResponseDto(true, Collections.singletonList(details), fsResponseDto);
    }
}
