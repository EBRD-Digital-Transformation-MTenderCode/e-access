package com.procurement.access.service;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.base.Strings;
import com.procurement.access.dao.FsDao;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.fs.FsDto;
import com.procurement.access.model.dto.fs.FsResponseDto;
import com.procurement.access.model.dto.ocds.RelatedProcess;
import com.procurement.access.model.entity.FsEntity;
import com.procurement.access.utils.DateUtil;
import com.procurement.access.utils.JsonUtil;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class FsServiceImpl implements FsService {

    private static final String SEPARATOR = "-";
    private final JsonUtil jsonUtil;
    private final DateUtil dateUtil;
    private final FsDao fsDao;

    public FsServiceImpl(final JsonUtil jsonUtil,
                         final DateUtil dateUtil,
                         final FsDao fsDao) {
        this.jsonUtil = jsonUtil;
        this.dateUtil = dateUtil;
        this.fsDao = fsDao;
    }

    @Override
    public ResponseDto createFs(final String cpId,
                                final String owner,
                                final FsDto fs) {
        final LocalDateTime addedDate = dateUtil.getNowUTC();
        fs.setDate(addedDate);
        fs.setId(UUIDs.timeBased().toString());
        setBudgetId(fs);
        final FsEntity entity = getEntity(cpId, owner, fs);
        fsDao.save(entity);
        return getResponseDto(cpId, entity.getToken(), fs);
    }


    @Override
    public ResponseDto updateFs( final String cpId,
                                 final String token,
                                 final String owner,
                                 final FsDto fsDto) {
        if (Strings.isNullOrEmpty(fsDto.getId())) throw new ErrorException("Invalid fs id.");
        final FsEntity entity = Optional.ofNullable(fsDao.getByCpIdAndIdAndToken(cpId, fsDto.getId(), token))
                .orElseThrow(() -> new ErrorException("Data not found."));
        if (!entity.getOwner().equals(owner)) throw new ErrorException("Invalid owner.");
        final FsDto fs = jsonUtil.toObject(FsDto.class, entity.getJsonData());
        fs.setPlanning(fsDto.getPlanning());
        fs.setTender(fsDto.getTender());
        entity.setJsonData(jsonUtil.toJson(fs));
        fsDao.save(entity);
        return getResponseDto(cpId, entity.getToken(), fs);
    }

    private void setBudgetId(final FsDto fs) {
        fs.getPlanning().getBudget().setId(getId());
    }


//    private String getIdentifier(final FsDto fs) {
//        final RelatedProcess relatedProcess = fs.getRelatedProcesses()
//                .stream()
//                .filter(rp -> rp.getRelationship().contains(RelatedProcess.RelatedProcessType.PARENT))
//                .filter(rp -> !rp.getIdentifier().isEmpty())
//                .findFirst()
//                .orElse(null);
//        if (Objects.isNull(relatedProcess)) {
//            throw new ErrorException("ocid in related processes not found.");
//        } else {
//            return relatedProcess.getIdentifier();
//        }
//    }

    private String getId() {
        return UUIDs.timeBased().toString();
    }

    private Double getAmount(final FsDto fs) {
        return fs.getPlanning().getBudget().getAmount().getAmount();
    }

    private FsEntity getEntity(final String cpId, final String owner, final FsDto fs) {
        final FsEntity fsEntity = new FsEntity();
        fsEntity.setCpId(cpId);
        fsEntity.setFsId(fs.getId());
        fsEntity.setToken(UUIDs.timeBased().toString());
        fsEntity.setOwner(owner);
        fsEntity.setJsonData(jsonUtil.toJson(fs));
        fsEntity.setAmount(getAmount(fs));
        return fsEntity;
    }

    private ResponseDto getResponseDto(final String cpId, final String token, final FsDto fs) {
        final FsResponseDto responseDto = new FsResponseDto(
                token,
                cpId,
                fs.getId(),
                fs.getDate(),
                fs.getTag(),
                fs.getInitiationType(),
                fs.getLanguage(),
                fs.getPlanning(),
                fs.getParties(),
                fs.getRelatedProcesses()
        );
        return new ResponseDto<>(true, null, responseDto);
    }

}
