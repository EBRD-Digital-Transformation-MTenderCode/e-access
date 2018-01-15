package com.procurement.access.service;

import com.datastax.driver.core.utils.UUIDs;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.fs.FsDto;
import com.procurement.access.model.dto.fs.FsRelatedProcessDto;
import com.procurement.access.model.dto.fs.FsResponseDto;
import com.procurement.access.model.entity.FsEntity;
import com.procurement.access.repository.FsRepository;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class FsServiceImpl implements FsService {

    private static final String SEPARATOR = "-";
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
    public ResponseDto createFs(final String country,
                                final String pmd,
                                final String stage,
                                final String owner,
                                final FsDto fs) {
        final LocalDateTime addedDate = dateUtil.getNowUTC();
        final String cpId = getIdentifier(fs);
        final String ocId = getOcId(cpId, "fs");
        fs.setOcId(ocId);
        fs.setDate(addedDate);
        setBudgetId(fs);
        final FsEntity entity = fsRepository.save(getEntity(cpId, fs, owner));
        return getResponseDto(fs, entity);
    }

    private void setBudgetId(final FsDto fs) {
        fs.getPlanning().getBudget().setId(getId());
    }

    private String getIdentifier(final FsDto fs) {
        final FsRelatedProcessDto relatedProcess = fs.getRelatedProcesses()
                .stream()
                .filter(rp -> rp.getRelationship().contains(FsRelatedProcessDto.RelatedProcessType.PARENT))
                .filter(rp -> !rp.getIdentifier().isEmpty())
                .findFirst()
                .orElse(null);
        if (Objects.isNull(relatedProcess)) {
            throw new ErrorException("ocid in related processes not found.");
        } else {
            return relatedProcess.getIdentifier();
        }
    }

    private String getOcId(final String cpId, final String stage) {
        return cpId + SEPARATOR + stage + SEPARATOR + dateUtil.getMilliNowUTC();
    }

    private String getId() {
        return UUIDs.timeBased().toString();
    }

    private Double getAmount(final FsDto fs) {
        return fs.getPlanning().getBudget().getAmount().getAmount();
    }

    private FsEntity getEntity(final String cpId, final FsDto fs, final String owner) {
        final FsEntity fsEntity = new FsEntity();
        fsEntity.setCpId(cpId);
        fsEntity.setOcId(fs.getOcId());
        fsEntity.setToken(UUIDs.timeBased().toString());
        fsEntity.setOwner(owner);
        fsEntity.setJsonData(jsonUtil.toJson(fs));
        fsEntity.setAmount(getAmount(fs));
        return fsEntity;
    }

    private ResponseDto getResponseDto(final FsDto fs, final FsEntity entity) {
        final FsResponseDto responseDto = new FsResponseDto(
                entity.getToken(),
                entity.getOcId(),
                fs.getId(),
                fs.getDate(),
                fs.getTag(),
                fs.getInitiationType(),
                fs.getLanguage(),
                fs.getPlanning(),
                fs.getParties(),
                fs.getRelatedProcesses()
        );
        return new ResponseDto(true, null, responseDto);
    }

    @Override
    public ResponseDto updateFs(final FsDto fs) {
        return null;
    }

}
